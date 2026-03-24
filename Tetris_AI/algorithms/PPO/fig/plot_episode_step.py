import matplotlib.pyplot as plt
import csv
import numpy as np

font2 = {'family': 'Times New Roman',
         'weight': 'normal',
         'size': 10,
         }


def plot_data(path, step, weight):  # path:路径；  step:截取数据长度； plot_step：截取数据步长； weight:平滑权重
    result = csv_to_numpy(path)
    result = np.array(result[0]).astype(float)
    run_steps = np.arange(0, step)  # 读取训练步数

    real_step = np.zeros_like(result)
    real_step[0] = result[0]
    for i in range(1, len(result)):
        real_step[i] = (result[i] - result[i - 1])

    smoothed = smoothing_tensorboard2(real_step, weight)
    return run_steps, smoothed


def smoothing_tensorboard2(x, smooth):
    x = x.copy()
    weight = smooth
    for i in range(1, len(x)):  # 平滑处理循环
        x[i] = (x[i - 1] * weight + x[i]) / (weight + 1)
        weight = (weight + 1) * smooth
    return x


def csv_to_numpy(path):
    csv_file = open(path)
    csv_reader_lines = csv.reader(csv_file)
    data = []
    for one_line in csv_reader_lines:
        data.append(one_line)
    return data


if __name__ == "__main__":
    # plt.figure(figsize=(5, 4.4))
    plt.rc('font', family='Times new Roman', size=12)
    plt.title('Steps per episode over training')
    plt.ylabel('Steps')
    plt.xlabel('Episodes')

    path1 = r"../train_process_data/step.csv"
    colors = ['c', 'deepskyblue', 'limegreen', 'brown', 'r', '', '', '', '', '', '', '', '', ]

    t_step = 12500
    plot_step = 100
    step1, smoothed1 = plot_data(path1, t_step, 0.99)

    step1_sampled = step1[::plot_step]
    smoothed1_sampled = smoothed1[::plot_step]
    plt.plot(step1_sampled, smoothed1_sampled, marker="s", linestyle='-', label='ppo_return', color=colors[0])
    plt.savefig('episode_step.pdf')
    plt.show()

    #
    # plt.ylim(0, 4500)
    # plt.xlim(0, 300)
    # plt.plot(step1_sampled, smoothed1_sampled, marker="s", linestyle='-', label='ppo_return', color=colors[0])
    # plt.fill_between(step1, smooth(mean1 + std1), smooth(mean1 - std1), color=colors[0], alpha=.1)
    # plt.savefig('PPO_buffer.pdf')
    # plt.show()
