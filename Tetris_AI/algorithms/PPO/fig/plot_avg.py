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


def plot_data(path, step, plot_step):  # path:路径；  step:截取数据长度； plot_step：截取数据步长；
    result = csv_to_numpy(path)
    result = np.array(result).astype(float)
    run_steps = np.arange(0, step)  # 读取训练步数
    mean = np.mean(result, axis=0)
    print(np.mean(mean[-150:]))
    std = np.std(result, axis=0)
    return run_steps, mean, std


if __name__ == "__main__":
    # plt.figure(figsize=(5, 4.3))
    plt.rc('font', family='Times new Roman', size=12)
    plt.ylabel('Average return', fontsize=12)
    plt.xlabel('Episodes')

    path1 = r"../train_process_data/return_12500.csv"
    t_step = 12500

    plot_step = 50
    colors = ['brown', 'deepskyblue', 'limegreen', 'c', 'r', '', '', '', '', '', '', '', '', ]

    step1, mean1, std1 = plot_data(path1, t_step, plot_step)
    plt.ylim(0, 6000)
    plt.xticks(np.arange(0, 12501, 2500))
    plt.xlim(0, 12500)

    plt.plot(step1[::plot_step], mean1[::plot_step], marker="s", linestyle='-', color=colors[0])
    plt.fill_between(step1[::plot_step], (mean1 + std1)[::plot_step], (mean1 - std1)[::plot_step], color=colors[0],
                     alpha=.1)

    # plt.legend()
    plt.savefig('PPO_trajectory.pdf')
    plt.show()
