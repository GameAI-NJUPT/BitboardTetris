from tqdm import tqdm
import numpy as np
import torch
import csv
import collections
import random
import matplotlib.pyplot as plt


class ReplayBuffer:
    def __init__(self, capacity):
        self.buffer = collections.deque(maxlen=capacity)

    def add(self, state, action, reward, next_state, done):
        self.buffer.append((state, action, reward, next_state, done))

    def sample(self, batch_size):
        transitions = random.sample(self.buffer, batch_size)
        state, action, reward, next_state, done = zip(*transitions)
        return np.array(state), action, reward, np.array(next_state), done

    def size(self):
        return len(self.buffer)


def moving_average(a, window_size):
    cumulative_sum = np.cumsum(np.insert(a, 0, 0))
    middle = (
                     cumulative_sum[window_size:] - cumulative_sum[:-window_size]
             ) / window_size
    r = np.arange(1, window_size - 1, 2)
    begin = np.cumsum(a[: window_size - 1])[::2] / r
    end = (np.cumsum(a[:-window_size:-1])[::2] / r)[::-1]
    return np.concatenate((begin, middle, end))


def train_on_policy_agent(env, agent, num_episodes):
    return_list = []
    with tqdm(total=int(num_episodes), desc="Iteration") as pbar:
        # 采样一局游戏
        for i_episode in range(int(num_episodes)):
            episode_return = 0
            transition_dict = {
                "states": [],
                "actions": [],
                "next_states": [],
                "rewards": [],
                "dones": [],
                "log_prob": [],
            }
            # state = env.reset(seed=37)[0]
            state = env.reset()[0]
            done = False
            while not done:
                action = agent.take_action(state)
                next_state, reward, terminated, truncated, _ = env.step(action)
                # log_prob = torch.log(agent.policy_net(state).gather(1, action))
                transition_dict["states"].append(state)
                transition_dict["actions"].append(action)
                transition_dict["next_states"].append(next_state)
                transition_dict["rewards"].append(reward)
                transition_dict["dones"].append(terminated | truncated)
                # transition_dict["log_prob"].append(log_prob)
                state = next_state
                episode_return += reward
                if terminated or truncated:
                    done = True
            return_list.append(episode_return)
            agent.update(transition_dict)
            if (i_episode + 1) % 10 == 0:
                pbar.set_postfix(
                    {
                        "episode": "%d" % num_episodes,
                        "return": "%.3f" % episode_return,
                    }
                )
            pbar.update(1)
    return return_list


def train_off_policy_agent(
        env, agent, num_episodes, replay_buffer, minimal_size, batch_size
):
    return_list = []
    with tqdm(total=int(num_episodes), desc="Iteration %d") as pbar:
        for i_episode in range(int(num_episodes)):
            episode_return = 0
            state = env.reset()[0]
            done = False
            while not done:
                action = agent.take_action(state)
                next_state, reward, terminated, truncated, _ = env.step(action)
                if terminated or truncated:
                    done = True
                replay_buffer.add(state, action, reward, next_state, done)
                state = next_state
                episode_return += reward
                if replay_buffer.size() > minimal_size:
                    b_s, b_a, b_r, b_ns, b_d = replay_buffer.sample(batch_size)
                    transition_dict = {
                        "states": b_s,
                        "actions": b_a,
                        "next_states": b_ns,
                        "rewards": b_r,
                        "dones": b_d,
                    }
                    agent.update(transition_dict)
            return_list.append(episode_return)
            if (i_episode + 1) % 10 == 0:
                pbar.set_postfix(
                    {
                        "episode": "%d" % num_episodes,
                        "return": "%.3f" % episode_return,
                    }
                )
            pbar.update(1)
    return return_list


def compute_advantage(gamma, lmbda, td_delta):
    td_delta = td_delta.detach().numpy()
    advantage_list = []
    advantage = 0.0
    for delta in td_delta[::-1]:
        advantage = gamma * lmbda * advantage + delta
        advantage_list.append(advantage)
    advantage_list.reverse()
    return torch.tensor(np.array(advantage_list), dtype=torch.float)


def writecsv(filename, list):
    fileloc = "../data/" + filename
    with open(fileloc, mode="a+", newline="") as reward_file:
        reward_writer = csv.writer(
            reward_file, delimiter=",", quotechar='"', quoting=csv.QUOTE_MINIMAL
        )
        reward_writer.writerow(list)


def myplot(returnList, env_name, alg_name):
    episodes_list = list(range(len(returnList)))
    plt.plot(episodes_list, returnList)
    plt.xlabel("Episodes")
    plt.ylabel("Returns")
    plt.title("{} on {}".format(alg_name, env_name))
    plt.show()

    mv_return = moving_average(returnList, 9)
    plt.plot(episodes_list, mv_return)
    plt.xlabel("Episodes")
    plt.ylabel("Returns")
    plt.title("{} on {}".format(alg_name, env_name))
    plt.show()
