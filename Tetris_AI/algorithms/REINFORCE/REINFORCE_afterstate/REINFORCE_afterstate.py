import numpy as np
import torch
import Tetris_env.tetris as env
from Tetris_AI.common.ReplayBuffer import ReplayBufferAll
from Tetris_AI.common.Net import PolicyNet_Afterstates as PolicyNet
from tqdm import tqdm
import pandas as pd


class REINFORCE:
    def __init__(self, learning_rate, gamma, device):
        self.actor = PolicyNet().to(device)
        self.optimizer = torch.optim.Adam(self.actor.parameters(), lr=learning_rate)
        self.gamma = gamma
        self.device = device
        self.tao_0 = 0.5
        self.tao_k = 0.00025
        self.temperature = 0.5

    def take_action(self, s, mask, is_test):
        s = torch.unsqueeze(torch.tensor(s, dtype=torch.float), 0).to(torch.device("cpu"))
        mask = torch.tensor(mask, dtype=torch.float).to(torch.device("cpu"))
        with torch.no_grad():
            probs = self.actor(s, mask, self.temperature)
            dist = torch.distributions.Categorical(probs=probs)
        if is_test:
            return torch.argmax(probs).item()
        else:
            return dist.sample().numpy()[0]

    def update(self, data_buffer):
        (features, n_features, a, rewards, dones,
         r_features, rn_features, mask) = data_buffer.list_to_tensor(self.device)

        G = torch.zeros(a.shape[0])
        G_Tmp = 0
        for i in reversed(range(len(rewards))):
            reward = rewards[i][0]
            G_Tmp = G_Tmp + reward
            G[i] = G_Tmp
        # G在view后已丢失grad，不需要detach
        G = G.view(a.shape[0], -1).to(torch.device("cuda"))

        log_prob = torch.log(self.actor(n_features, mask, self.temperature).gather(1, a)).to(torch.device("cuda"))
        loss = -log_prob * G
        loss = torch.sum(loss)
        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()


def REINFORCE_train(env, agent, num_episodes, buffer):
    return_list = []
    with tqdm(total=int(num_episodes), desc="Iteration") as pbar:
        # 采样一局游戏
        for episode in range(1, int(num_episodes) + 1):
            agent.temperature = agent.tao_0 / (1 + agent.tao_k * episode)
            state = env.reset()
            feature = np.zeros(306)
            action = 0
            done = False
            buffer.reset_buffer()
            while not done:
                real_features = feature[action * 9:(action + 1) * 9]
                next_feature, mask = env.get_9featureAndMask(state)
                action = agent.take_action(next_feature, mask, False)
                next_state, reward, done = env.step(state, action)
                buffer.store(feature, next_feature, action, reward, done, real_features, mask)
                feature = next_feature
                state = next_state
            return_list.append(state[11])
            agent.update(buffer)
            if episode % 50 == 0:
                pbar.set_postfix(
                    {
                        "return": "%.2f" % (sum(return_list[-50:]) / 50)
                    }
                )
            if episode % 2500 == 0:
                return_data = pd.DataFrame([return_list])
                return_data.to_csv(r"train_process_data/return_" + str(episode) + ".csv", mode='w', index=False,
                                   header=False)
                torch.save(agent.actor.state_dict(), "model/actor_" + str(episode) + ".pt")
            pbar.update(1)
    return return_list


if __name__ == "__main__":
    tetris = env.Tetris()

    learning_rate = 1e-3
    num_episodes = 10000
    gamma = 1
    device = torch.device("cpu")
    env_name = "Tetris"
    alg_name = "REINFORCE"
    buffer = ReplayBufferAll(9, 1024)
    agent = REINFORCE(learning_rate, gamma, device)
    return_list = REINFORCE_train(tetris, agent, num_episodes, buffer)
