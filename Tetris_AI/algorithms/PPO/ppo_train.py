import torch
import time
import torch.nn.functional as F
import Tetris_AI.utils.rl_utils as rl_utils
from Tetris_AI.common.Net import PolicyNet_Afterstates, ValueNet
from Tetris_AI.common.ReplayBuffer import ReplayBufferAll
import Tetris_env.tetris as env
from tqdm import tqdm
import pandas as pd
import numpy as np


class PPO:
    # PPO (clipped) for a discrete action space: actor outputs a 34-action policy, critic evaluates afterstate value
    def __init__(self, actor_lr, critic_lr, device):
        self.actor = PolicyNet_Afterstates().to(device)
        self.critic = ValueNet(9).to(device)
        self.actor_optimizer = torch.optim.Adam(self.actor.parameters(), lr=actor_lr)
        self.critic_optimizer = torch.optim.Adam(self.critic.parameters(), lr=critic_lr)
        self.gamma = 0.99
        self.lambda_ = 0.99
        self.epochs = 10
        self.eps = 0.1
        self.device = device
        self.tao_0 = 0.5
        self.tao_k = 0.00025
        self.temperature = 0.5

    def take_action(self, s, mask, is_test):
        # Sample or greedy-select an action; mask filters invalid actions (zeros -> -inf)
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
        # Update actor/critic using GAE and clipped surrogate objective
        (features, n_features, r_action, rewards, dones,
         r_features, rn_features, mask) = data_buffer.list_to_tensor(self.device)

        td_target = rewards + self.gamma * self.critic(rn_features) * (1 - dones)  # TD target
        td_delta = td_target - self.critic(r_features)  # TD residual
        advantage = rl_utils.compute_advantage(self.gamma, self.lambda_, td_delta.cpu()).to(self.device)  # GAE
        old_log_probs = torch.log(self.actor(n_features, mask, self.temperature).gather(1, r_action))  # old log prob
        old_log_probs = old_log_probs.detach()
        for _ in range(self.epochs):
            log_probs = torch.log(self.actor(n_features, mask, self.temperature).gather(1, r_action))  # new log prob
            ratio = torch.exp(log_probs - old_log_probs)  # probability ratio
            surr1 = ratio * advantage 
            surr2 = torch.clamp(ratio, 1 - self.eps, 1 + self.eps) * advantage 
            actor_loss = torch.mean(-torch.min(surr1, surr2))  # actor loss
            critic_loss = torch.mean(F.mse_loss(self.critic(r_features), td_target.detach()))  # value regression
            self.actor_optimizer.zero_grad()
            self.critic_optimizer.zero_grad()
            actor_loss.backward()
            critic_loss.backward()
            self.actor_optimizer.step()
            self.critic_optimizer.step()


def ppo_train(env, agent, num_episodes, buffer):
    return_list = []  # per-episode score (state[11])
    step_list = []
    sample_sum = []
    update_sum = []
    total_step = 0
    with tqdm(total=int(num_episodes), desc="Iteration") as pbar:
        # Sample one full episode
        for episode in range(1, int(num_episodes) + 1):
            agent.temperature = agent.tao_0 / (1 + agent.tao_k * episode)
            state = env.reset()
            feature = np.zeros(306)
            action = 0
            done = False
            sample_start = time.perf_counter()
            buffer.reset_buffer()
            while not done:
                total_step += 1
                real_features = feature[action * 9:(action + 1) * 9]  # 9-d afterstate features for the taken action
                next_feature, mask = env.get_9featureAndMask(state)  # compute 34*9 features and valid action mask
                action = agent.take_action(next_feature, mask, False)
                next_state, reward, done = env.step(state, action)
                buffer.store(feature, next_feature, action, reward, done, real_features, mask)
                feature = next_feature
                state = next_state
            return_list.append(state[11])
            step_list.append(total_step)
            sample_end = time.perf_counter()
            update_start = time.perf_counter()
            agent.update(buffer)  # update once using this episode's trajectory
            update_end = time.perf_counter()
            sample_sum.append(sample_end - sample_start)
            update_sum.append(update_end - update_start)
            if episode % 50 == 0:
                pbar.set_postfix(
                    {
                        "return": "%.2f" % (sum(return_list[-50:]) / 50)
                    }
                )
            if episode % 2500 == 0:
                # Periodically save training curves and model weights
                return_data = pd.DataFrame([return_list])
                return_data.to_csv(return_savePath + str(episode) + ".csv", mode='w', index=False, header=False)
                step_data = pd.DataFrame([step_list])
                step_data.to_csv("./train_process_data/step.csv", mode='w', index=False, header=False)
                sample_sum_data = pd.DataFrame([sample_sum])
                sample_sum_data.to_csv("./train_process_data/sample_time.csv", mode='w', index=False, header=False)
                update_sum_data = pd.DataFrame([update_sum])
                update_sum_data.to_csv("./train_process_data/update_time.csv", mode='w', index=False, header=False)
                torch.save(agent.actor.state_dict(), "./model/actor_" + str(episode) + ".pt")
                torch.save(agent.critic.state_dict(), "./model/critic_" + str(episode) + ".pt")
            pbar.update(1)
    return return_list


if __name__ == "__main__":
    tetris = env.Tetris()
    actor_lr = 3e-4
    critic_lr = 3e-4
    device = torch.device("cpu")
    num_episodes = 12500
    alg_name = "PPO"
    env_name = "Tetris"
    agent = PPO(actor_lr, critic_lr, device)
    buffer = ReplayBufferAll(9, 1024)
    return_savePath = r"./train_process_data/return_"
    return_list = ppo_train(tetris, agent, num_episodes, buffer)
