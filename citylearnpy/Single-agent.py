import warnings

#加载NormalizedObservationWrapper  观察值归一化处理器
#加载ClippedObservationWrapper 观察值裁剪器
#加载RLlibSingleAgentWrapper  单个agent的环境包装器
from citylearn.wrappers import ClippedObservationWrapper, NormalizedObservationWrapper, RLlibSingleAgentWrapper

#引入SAC算法配置
from ray.rllib.algorithms.sac import SACConfig as Config

#引入数据分析库
import pandas as pd

# 设置显示所有列
pd.set_option('display.max_columns', None)
pd.set_option('display.width', None)


# 忽略部分警告
warnings.filterwarnings('ignore', category=DeprecationWarning)

# 初始化环境配置
#确定加载NormalizedObservationWrapper、ClippedObservationWrapper
#确定使用citylearn_challenge_2023_phase_2_local_evaluation数据集
env_config = {
    'env_kwargs': {
        'schema': 'citylearn_challenge_2023_phase_2_local_evaluation',
    },
    'wrappers': [
        NormalizedObservationWrapper,
        ClippedObservationWrapper
    ]
}
#创建SAC算法的默认配置模板
config = (
    Config()
    .environment(RLlibSingleAgentWrapper, env_config=env_config)
)
#构建
model = config.build()

# 训练两轮
for i in range(2):
    _ = model.train()

# 加载原始数据
env = RLlibSingleAgentWrapper(env_config)
# 环境初始化
observations, _ = env.reset()

#评估循环
while not env.unwrapped.terminated:
    #不添加随机噪声（确定性策略）
    actions = model.compute_single_action(observations, explore=False)
    # 执行动作 观察值更新
    observations, _, _, _, _ = env.step(actions)

#调用评估方法
kpis = env.unwrapped.evaluate()
#数据格式化
kpis = kpis.pivot(index='cost_function', columns='name', values='value').round(3)
#数据清洗
kpis = kpis.dropna(how='all')
print("outputkpi")
print(kpis)