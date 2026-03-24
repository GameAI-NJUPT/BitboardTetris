import Tetris_env.tetris as env
import numpy as np
import torch
from Tetris_AI.algorithms.PPO.ppo_train import PPO
from tqdm import tqdm

# 用于确定DT-9特征的范围
if __name__ == "__main__":
    env = env.Tetris()
    actor_lr = 3e-4
    critic_lr = 3e-4
    device = torch.device("cpu")
    agent = PPO(actor_lr, critic_lr, device)
    agent.actor.load_state_dict(
        torch.load(r"../Tetris_AI/algorithms/ppo/model/actor_2500.pt"))
    feature_value_counts = [set() for _ in range(9)]
    for i in tqdm(range(1), desc="Episode Progress"):
        state = env.reset()
        feature = np.zeros(306)
        action = 0
        done = False
        while not done:
            real_features = feature[action * 9:(action + 1) * 9]
            next_feature, mask = env.get_9featureAndMask(state)
            valid_actions = np.where(mask)[0]
            for j in range(len(valid_actions)):
                temp_feature = next_feature[valid_actions[j] * 9:(valid_actions[j] + 1) * 9]
                for k in range(9):
                    feature_value_counts[k].add(temp_feature[k])
            action = agent.take_action(next_feature, mask, True)
            next_state, reward, done = env.step(state, action)
            feature = next_feature
            state = next_state
        print(state[11])
    sorted_feature_value_counts = []
    for feature_set in feature_value_counts:
        sorted_set = sorted(feature_set)
        sorted_feature_value_counts.append(sorted_set)

    print(sorted_feature_value_counts)
