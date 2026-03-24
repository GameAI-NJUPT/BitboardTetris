import csv
import numpy as np


def csv_to_numpy(path):
    csv_file = open(path)
    csv_reader_lines = csv.reader(csv_file)
    data = []
    for one_line in csv_reader_lines:
        data.append(one_line)
    return np.array(data[0]).astype(float)


if __name__ == '__main__':
    path_sample = r"sample_time.csv"
    path_update = r"update_time.csv"
    sample_data = csv_to_numpy(path_sample)
    update_data = csv_to_numpy(path_update)
    print(np.sum(sample_data))
    print(np.sum(update_data))
