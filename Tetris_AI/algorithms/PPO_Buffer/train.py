import argparse
from replaybuffer import ReplayBuffer
from ppo_discrete import PPO_discrete
import Tetris_env.tetris as env
import jpype
import numpy as np
import pandas as pd
import torch
import time
from tqdm import tqdm


def main(args, env):
    args.state_dim = 9
    args.action_dim = 34

    total_steps = 0
    episode = 0
    replay_buffer = ReplayBuffer(args)
    agent = PPO_discrete(args)
    test_list = []
    sample_time = []
    update_time = []
    while total_steps < args.max_train_steps:
        currentState = env.reset()
        currentFeature = np.zeros(306)
        a = 0
        done = False
        while not done:
            sample_start = time.perf_counter()
            rFeature = currentFeature[a * 9:(a + 1) * 9]
            total_steps += 1
            nextFeature, mask = env.get_9feature(currentState)
            a, a_logprob = agent.choose_action(nextFeature, mask)
            nextState, reward, done = env.step(currentState, a)
            replay_buffer.store(currentFeature, nextFeature, a, a_logprob, reward, done, rFeature, mask)
            currentFeature = nextFeature
            currentState = nextState
            sample_end = time.perf_counter()
            sample_time.append(sample_end - sample_start)

            if replay_buffer.count == args.batch_size:
                episode += 1
                update_start = time.perf_counter()
                agent.update(replay_buffer, total_steps)
                update_end = time.perf_counter()
                update_time.append(update_end - update_start)
                replay_buffer.count = 0
                theta = agent.actor.linear_layer.weight.data.cpu().numpy().flatten()
                theta = jpype.JArray(jpype.JDouble, 1)(theta.tolist())
                tetris.vf.setTheta(theta)
                test_list.append(env.parallel_episode())
                print(episode, test_list[-1])

            if total_steps == args.max_train_steps:
                break

        # return_data = pd.DataFrame([test_list])
        # return_data.to_csv("train_process_data/return.csv", mode='w', index=False, header=False)
        # torch.save(agent.actor.state_dict(), "model/actor.pt")
        # torch.save(agent.critic.state_dict(), "model/critic.pt")
        # sample_sum_data = pd.DataFrame([sample_time])
        # sample_sum_data.to_csv("train_process_data/sample_time.csv", mode='w', index=False, header=False)
        # update_sum_data = pd.DataFrame([update_time])
        # update_sum_data.to_csv("train_process_data/update_time.csv", mode='w', index=False, header=False)

if __name__ == "__main__":
    parser = argparse.ArgumentParser("Hyperparameter Setting for PPO_buffer")
    parser.add_argument(
        "--max_train_steps",
        type=int,
        default=int(2048 * 300),
        help=" Maximum number of training steps",
    )
    parser.add_argument("--batch_size", type=int, default=2048, help="Batch size")
    parser.add_argument(
        "--mini_batch_size", type=int, default=256, help="Minibatch size"
    )
    parser.add_argument(
        "--lr_a", type=float, default=3e-4, help="Learning rate of actor"
    )
    parser.add_argument(
        "--lr_c", type=float, default=3e-4, help="Learning rate of critic"
    )
    parser.add_argument("--gamma", type=float, default=0.99, help="Discount factor")
    parser.add_argument("--lamda", type=float, default=0.99, help="GAE parameter")
    parser.add_argument("--epsilon", type=float, default=0.2, help="PPO clip parameter")
    parser.add_argument("--K_epochs", type=int, default=10, help="PPO parameter")
    parser.add_argument(
        "--use_adv_norm",
        type=bool,
        default=True,
        help="Trick 1:advantage normalization",
    )
    parser.add_argument(
        "--use_state_norm", type=bool, default=False, help="Trick 2:state normalization"
    )
    parser.add_argument(
        "--use_reward_norm",
        type=bool,
        default=False,
        help="Trick 3:reward normalization",
    )
    parser.add_argument(
        "--use_reward_scaling", type=bool, default=False, help="Trick 4:reward scaling"
    )
    parser.add_argument(
        "--entropy_coef", type=float, default=0.01, help="Trick 5: policy entropy"
    )
    parser.add_argument(
        "--use_lr_decay", type=bool, default=True, help="Trick 6:learning rate Decay"
    )
    parser.add_argument(
        "--use_grad_clip", type=bool, default=True, help="Trick 7: Gradient clip"
    )
    parser.add_argument(
        "--use_orthogonal_init",
        type=bool,
        default=True,
        help="Trick 8: orthogonal initialization",
    )
    parser.add_argument(
        "--set_adam_eps",
        type=float,
        default=True,
        help="Trick 9: set Adam epsilon=1e-5",
    )
    parser.add_argument(
        "--use_tanh",
        type=float,
        default=True,
        help="Trick 10: tanh activation function",
    )

    args = parser.parse_args()
    tetris = env.Tetris()
    main(args, tetris)
