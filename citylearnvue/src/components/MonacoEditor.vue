<template>
  <div class="python-editor">
    <div class="editor-header">
      <h3>{{ fileName }}</h3>
      <div class="controls">
        <button  @click="saveCode" :disabled="isSaving || null==this.currentFile || !this.currentFile?.ifEdit" class="btn-save">
          {{ isSaving ? '保存中...' : '保存' }}
        </button>
       
      </div>
    </div>

    <div class="editor-layout">
      <div class="file-list">
        <div class="file-list-content">
          <h4>文件列表
            <button style="float: right;" @click.stop="addPyFile()" class="rename-btn"><i class="el-icon-folder-add"></i></button>
            
          </h4>
          <ul v-if="files.length">
            <li 
              v-for="file in files" 
              :key="file.id"
              :class="{ active: file.id === currentFileId }"
            >
              <div v-if="file.id !== editingFileId" class="file-item">
                <span @click="selectFile(file)">
                  <div style="color:chocolate" v-show="file.ifEdit"><i v-show="file.ifSystem" class="el-icon-star-off"></i>{{ file.fileName }}</div>
                  <div v-show="!file.ifEdit"><i v-show="file.ifSystem" class="el-icon-star-off"></i>{{ file.fileName }}</div>
                </span>
                <button @click.stop="startEditing(file)" class="rename-btn"><i class="el-icon-edit"></i></button>
                <button @click.stop="deletePyFile(file)" class="rename-btn"><i class="el-icon-folder-delete"></i></button>
              </div>
              <div v-else class="edit-mode">
                <input 
                  v-model="editingFileName" 
                  @keyup.enter="confirmRename"
                  ref="renameInput"
                >
                <button @click="confirmRename" class="confirm-btn">√</button>
                <button @click="cancelEditing" class="cancel-btn">×</button>
              </div>
            </li>
          </ul>
        </div>

        <div class="file-description">
          <h4>文件简介</h4>
          <textarea 
            v-model="currentFileDescription" 
            placeholder="添加文件简介..."
            @blur="updateFileDescription()"
          ></textarea>
        </div>
      </div>

      <div class="editor-area">
        <div ref="editorContainer" class="editor-container"></div>
        
        <div v-if="output" class="output-panel">
          <div class="output-header">
            <span>输出</span>
            <button @click="output = ''" class="btn-clear">清空</button>
          </div>
          <pre class="output-content">{{ output }}</pre>
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor';
import axios from 'axios';

export default {
  name: 'PythonEditor',
  props: {
    /* eslint-disable */
    filePath: {
      type: String,
      required: true
    },
    apiBaseUrl: {
      type: String,
      default: process.env.VUE_APP_API_BASE_URL || '/api'
    }
  },
  data() {
    return {
      editor: null,
      code: '',
      isSaving: false,
      isRunning: false,
      output: '',
      error: '',
      fileName: '',
      files: [],
      currentFileId: null,
      currentFile: null,
      currentFileDescription: '',
      isLoadingFiles: false,
      editingFileId: null,
      editingFileName: '',
      editingFileOriginalName: ''
    };
  },
  computed: {
    fileId() {
      return btoa(this.filePath); // 简单的路径编码
    }
  },
  watch: {
    filePath: {
      immediate: true,
      handler(newPath) {
        if (newPath) {
          this.fileName = newPath.split('/').pop();
          this.loadCode();
        }
      }
    }
  },
  mounted() {
    this.initEditor();
    this.fetchFiles();
    window.addEventListener('resize', this.handleResize);
  },
  beforeUnmount() {
    if (this.editor) {
      this.editor.dispose();
    }
    window.removeEventListener('resize', this.handleResize);
  },
  methods: {
    async fetchFiles() {
      this.isLoadingFiles = true;
      axios.get('/api/web/basedata/getPyFileList', {
      })
      .then(response => {
        this.files = response.data.data;
      })
      .catch(error => {
        console.error('获取数据失败:', error);
        this.$message.error('数据加载失败');
      }).finally(() => {
        this.isLoadingFiles = false;
      });
    },
    
    selectFile(file) {
      this.currentFileId = file.id;
      this.currentFile = file;
      this.currentFileDescription = file.description;
      this.filePath = file.path;
      this.fileName = file.name;
      this.loadCode();
    },
    async deletePyFile(file){
      try {
        const response = await axios.post('/api/web/basedata/deletePyFile', {
          id: file.id,
        });
         console.log(response.data)
        if (response.data.code==0) {
          this.files.splice(this.files.findIndex(item => item.id === file.id), 1); 
        } else {
          this.$message.error(response.data.message || '删除失败');
        }
      } catch (error) {
        console.error('删除失败:', error);
        this.$message.error('删除失败');
      } 
    },
    async addPyFile(){
      if(this.isSaving==true){
        return
      }
      this.isSaving = true;
      try {
        const response = await axios.post('/api/web/basedata/addPyFile', {});
         console.log(response.data)
        if (response.data.code==0) {
          this.files.push(response.data.data)
        } else {
          this.$message.error(response.data.message || '添加失败');
        }
      } catch (error) {
        console.error('添加失败:', error);
        this.$message.error('添加失败');
      } finally {
        setTimeout(() => {
          this.isSaving = false;
        }, 1000);
        
      }
    },

    async updateFileDescription(){
      if(null==this.currentFile){
        return;
      }
      this.currentFile.description=this.currentFileDescription
      console.log(this.currentFile.description)
       console.log(this.currentFileDescription)
      try {
        const response = await axios.post('/api/web/basedata/savePyFile', {
          id: this.currentFile.id,
          description:this.currentFile.description
        });
        console.log(response.data)
        if (response.data.code==0) {
        } else {
          this.$message.error(response.data.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败');
      }
    },
    
    initEditor() {
      this.editor = monaco.editor.create(this.$refs.editorContainer, {
        value: '# 加载中...\n',
        language: 'python',
        theme: 'vs-dark',
        automaticLayout: false, // 我们手动处理布局
        minimap: { enabled: false },
        fontSize: 14,
        lineNumbers: 'on',
        folding: true,
        scrollBeyondLastLine: false,
        wordWrap: 'on',
        lineNumbersMinChars: 3,
        renderLineHighlight: 'all',
        scrollbar: {
          vertical: 'auto',
          horizontal: 'auto'
        }
      });
      
      // 监听内容变化
      this.editor.onDidChangeModelContent(() => {
        if(this.currentFile.code!=this.editor.getValue()){
          this.currentFile.code=this.editor.getValue();
          if(this.currentFile.oriCode!=this.currentFile.code){
            this.currentFile.ifEdit=true;
            this.$forceUpdate();
          }else{
            this.currentFile.ifEdit=false;
            this.$forceUpdate();
          }
        }
        
        //alert(this.currentFile.ifEdit)
        
      });
    
    },
    
    async loadCode() {
      if(null==this.currentFile || null==this.currentFile.code || this.currentFile.code==''){
        axios.get('/api/web/basedata/getPyFile', {
        params: {
          id: this.currentFileId,
        }
      })
      .then(response => {
         const code = response.data.data;
         this.currentFile.code=code;
         this.currentFile.oriCode=code;
         this.currentFile.ifEdit=false;
          this.editor.setValue(this.currentFile.code);
          this.code = code;
          this.error = '';
      })
      .catch(error => {
        console.error('获取数据失败:', error);
        this.$message.error('数据加载失败');
      })
      }else{
        this.editor.setValue(this.currentFile.code);
        this.code = this.currentFile.code;
          this.error = '';
      }
      
    },
    
    async saveCode() {
      this.isSaving = true;
      this.error = '';
      
      try {
        const response = await axios.post('/api/web/basedata/savePyFile', {
          id: this.currentFile.id,
          code:this.currentFile.code
        });
        console.log(response.data)
        if (response.data.code==0) {
          this.currentFile.oriCode=this.currentFile.code;
          this.currentFile.ifEdit=false;
          this.$message.success('保存成功');
        } else {
          this.$message.error(response.data.message || '保存失败');
        }
      } catch (error) {
        console.error('保存失败:', error);
        this.$message.error('保存失败');
      } finally {
        this.isSaving = false;
      }
    },
    
    async runCode() {
      this.isRunning = true;
      this.output = '';
      this.error = '';
      
      try {
        const response = await fetch(`${this.apiBaseUrl}/code/run`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            code: this.code
          })
        });
        
        const result = await response.json();
        
        if (response.ok) {
          this.output = result.output || '代码执行成功，但没有输出';
        } else {
          this.output = result.error || '执行失败';
        }
      } catch (err) {
        this.error = `执行失败: ${err.message}`;
        console.error('执行代码失败:', err);
      } finally {
        this.isRunning = false;
      }
    },
    
    handleResize() {
      if (this.editor) {
        this.editor.layout();
      }
    },
    
    startEditing(file) {
      this.editingFileId = file.id;
      this.editingFileName = file.fileName;
      this.editingFileOriginalName = file.fileName;
      this.$nextTick(() => {
        this.$refs.renameInput?.focus();
      });
    },
    
    async confirmRename() {
      if (!this.editingFileName.trim()) {
        this.$message.error('文件名不能为空');
        return;
      }
       console.log(this.editingFileName)
      if (this.editingFileName==this.editingFileOriginalName) {
        return;
      }
     
      try {
        const response = await axios.post('/api/web/basedata/savePyFile', {
          id: this.editingFileId,
          fileName: this.editingFileName
        });
        console.log(response)
        if (response.data.code==0) {
          const file = this.files.find(f => f.id === this.editingFileId);
          if (file) {
            file.fileName = this.editingFileName;
            this.$message.success('重命名成功');
          }
        } else {
          //NOCONTROL.py
          this.$message.error(response.data.message || '重命名失败');
        }
      } catch (error) {
        console.error('重命名失败:', error);
        this.$message.error('重命名失败');
      } finally {
        this.cancelEditing();
      }
    },
    
    cancelEditing() {
      this.editingFileId = null;
      this.editingFileName = '';
      this.editingFileOriginalName = '';
    }
  }
};
</script>

<style scoped>
.python-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;
  border-radius: 8px;
  overflow: hidden;
}

.editor-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.file-list {
  width: 20%;
  background: #252526;
  border-right: 1px solid #1a1a1a;
  padding: 10px;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

.file-list-content {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 150px; /* 为简介区域预留空间 */
}

.file-description {
  position: absolute;
  bottom: 30px;  /* 增加底部间距 */
  left: 10px;
  right: 10px;
  background: #252526;
  
  border-top: 1px solid #3e3e42;
  
}

.file-description textarea {
  width: 100%;
  min-height: 100px;
  resize: none; /* 禁用缩放功能 */
}

.file-list h4 {
  color: #cccccc;
  margin: 0 0 10px 0;
  padding: 0 0 5px 0;
  border-bottom: 1px solid #3e3e42;
}

.file-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.file-list li {
  padding: 8px 10px;
  color: #d4d4d4;
  border-radius: 3px;
  margin-bottom: 2px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.file-item span {
  cursor: pointer;
  flex-grow: 1;
}

.rename-btn {
  background: transparent;
  color: #999;
  border: none;
  cursor: pointer;
  padding: 2px 5px;
  margin-left: 5px;
  font-size: 12px;
}

.rename-btn:hover {
  color: #ccc;
  background: rgba(255,255,255,0.1);
}



.edit-mode {
  display: flex;
  align-items: center;
  gap: 5px;
}

.edit-mode input {
  flex-grow: 1;
  background: #333;
  border: 1px solid #555;
  color: #fff;
  padding: 3px 5px;
  border-radius: 3px;
}

.confirm-btn, .cancel-btn {
  background: transparent;
  border: none;
  color: #ccc;
  cursor: pointer;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 3px;
}

.confirm-btn:hover {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.cancel-btn:hover {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
}

.file-list li:hover {
  background: #2a2d2e;
}

.file-list li.active {
  background: #37373d;
}

.loading {
  color: #999;
  text-align: center;
  padding: 10px;
}

.editor-area {
  width: 80%;
  display: flex;
  flex-direction: column;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #2d2d30;
  color: #cccccc;
  border-bottom: 1px solid #3e3e42;
}

.editor-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.controls {
  display: flex;
  gap: 8px;
}

.controls button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.controls button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-save {
  background: #0e639c;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #1177bb;
}

.btn-run {
  background: #55aa55;
  color: white;
}

.btn-run:hover:not(:disabled) {
  background: #66bb66;
}

.editor-container {
  flex: 1;
  min-height: 300px;
}

.output-panel {
  border-top: 1px solid #3e3e42;
  background: #1e1e1e;
  max-height: 200px;
  display: flex;
  flex-direction: column;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #2d2d30;
  color: #cccccc;
  font-weight: 500;
}

.btn-clear {
  background: transparent;
  color: #cccccc;
  border: 1px solid #5a5a5a;
  border-radius: 4px;
  padding: 4px 8px;
  cursor: pointer;
  font-size: 12px;
}

.btn-clear:hover {
  background: #3c3c3c;
}

.output-content {
  flex: 1;
  padding: 12px 16px;
  margin: 0;
  overflow: auto;
  color: #d4d4d4;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.4;
  white-space: pre-wrap;
}

.error-message {
  padding: 12px 16px;
  background: #c75050;
  color: white;
  font-size: 14px;
}
</style>