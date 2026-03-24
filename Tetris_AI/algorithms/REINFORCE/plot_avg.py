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
    print(np.mean(mean[-50:]))
    std = np.std(result, axis=0)
    return run_steps[::plot_step], mean[::plot_step], std[::plot_step]


if __name__ == "__main__":
    # plt.figure(figsize=(5, 4.3))
    plt.rc('font', family='Times new Roman', size=12)
    plt.ylabel('Average return', fontsize=12)
    plt.xlabel('Episodes')

    path1 = r"return_base.csv"
    path2 = r"return_qsa.csv"
    t_step = 10000

    plot_step = 50
    colors = ['brown', 'deepskyblue', 'limegreen', 'c', 'r', '', '', '', '', '', '', '', '', ]

    step1, mean1, std1 = plot_data(path1, t_step, plot_step)
    step2, mean2, std2 = plot_data(path2, t_step, plot_step)
    plt.ylim(0, 5000)
    plt.xticks(np.arange(0, 10001, 2500))
    plt.xlim(0, 10000)

    plt.plot(step1, mean1, marker="s", linestyle='-', label='REINFORCE(Evaluate afterstate)', color=colors[0])
    plt.fill_between(step1, mean1 + std1, mean1 - std1, color=colors[0], alpha=.1)

    plt.plot(step2, mean2, marker="*", linestyle='-', label='REINFORCE(Evaluate action)', color=colors[1])
    plt.fill_between(step2, mean2 + std2, mean2 - std2, color=colors[1], alpha=.1)
    plt.legend()
    plt.savefig('REINFORCE.pdf')
    plt.show()
