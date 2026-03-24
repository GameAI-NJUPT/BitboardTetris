import matplotlib.pyplot as plt
import numpy as np
import csv

font2 = {'family': 'Times New Roman',
         'weight': 'normal',
         'size': 10,
         }


def csv_to_numpy(path):
    csv_file = open(path)
    csv_reader_lines = csv.reader(csv_file)
    data = []
    for one_line in csv_reader_lines:
        data.append(one_line)
    return data


def plot_data(path, step, weight):  # path:路径；  step:截取数据长度； plot_step：截取数据步长； weight:平滑权重
    result = csv_to_numpy(path)
    result = np.array(result).astype(float)
    run_steps = np.arange(0, step)  # 读取训练步数
    mean = np.mean(result, axis=0)

    smoothed = smoothing_tensorboard2(mean, weight)
    std = np.std(result, axis=0)
    return run_steps, smoothed, mean, std


def plot_data_all(path, step):
    result = csv_to_numpy(path)
    result = np.array(result).astype(float)
    run_steps = np.arange(0, step)
    for i in range(result.shape[0]):
        y = smoothing_tensorboard2(result[i], 0.99)
        plt.plot(run_steps, y, label=str(i))
    plt.legend()
    plt.show()


def smoothing_tensorboard2(x, smooth):
    x = x.copy()
    weight = smooth
    for i in range(1, len(x)):  # 平滑处理循环
        x[i] = (x[i - 1] * weight + x[i]) / (weight + 1)
        weight = (weight + 1) * smooth
    return x


def moving_average(interval, windowsize):
    window = np.ones(int(windowsize)) / float(windowsize)
    re = np.convolve(interval, window, 'same')
    return re


def smooth(yValue, weight=0.85):
    smoothY = []
    last = yValue[0]
    for k in range(len(yValue)):
        smoothed_y = last * weight + (1 - weight) * yValue[k]
        smoothY.append(smoothed_y)
        last = smoothed_y
    return smoothY


if __name__ == "__main__":
    plt.figure(figsize=(5, 4.3))
    plt.rc('font', family='Times new Roman', size=12)
    plt.title('PPO train with buffer on Tetris')
    plt.ylabel('Average return', fontsize=12)
    plt.xlabel('Steps (×2048)')

    path1 = r"../train_process_data/return_combine.csv"

    t_step = 300

    plot_step = 5
    colors = ['c', 'deepskyblue', 'limegreen', 'brown', 'r', '', '', '', '', '', '', '', '', ]

    step1, smoothed1, mean1, std1 = plot_data(path1, t_step, 0.9)
    step1_sampled = step1[::plot_step]
    smoothed1_sampled = smoothed1[::plot_step]

    plt.ylim(0, 4500)
    plt.xlim(0, 300)
    plt.plot(step1_sampled, smoothed1_sampled, marker="s", linestyle='-', label='ppo_return', color=colors[0])
    plt.fill_between(step1, smooth(mean1 + std1), smooth(mean1 - std1), color=colors[0], alpha=.1)
    plt.savefig('episode_step.pdf')
    plt.show()
