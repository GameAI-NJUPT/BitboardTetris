import torch
import numpy as np


class ReplayBufferAll:
    # Episode-level trajectory buffer: stores "all actions' features + taken-action features"
    def __init__(self, feature_dim, init_size):
        # All actions' features per step: n * 34 * feature_dim
        self.s = np.zeros((init_size, 34 * feature_dim))
        self.s_ = np.zeros((init_size, 34 * feature_dim))
        # Taken action: n * 1
        self.reala = np.zeros((init_size, 1))
        # Reward: n * 1
        self.r = np.zeros((init_size, 1))
        # Done flag: n * 1
        self.done = np.zeros((init_size, 1))
        # Real (afterstate) features for taken action: n * feature_dim
        self.rs = np.zeros((init_size, feature_dim))
        # Action mask: n * 34
        self.mask = np.zeros((init_size, 34))
        self.length = 0
        self.capacity = init_size
        self.feature_dim = feature_dim

    def sample(self, batch_size, device):
        assert self.length >= batch_size, "Not enough samples in buffer"

        idx = np.random.choice(self.length, batch_size, replace=False)

        s = torch.tensor(self.s[idx], dtype=torch.float32).to(device)
        s_ = torch.tensor(self.s_[idx], dtype=torch.float32).to(device)
        a = torch.tensor(self.reala[idx], dtype=torch.int64).view(-1, 1).to(device)
        r = torch.tensor(self.r[idx], dtype=torch.float32).view(-1, 1).to(device)
        done = torch.tensor(self.done[idx], dtype=torch.float32).view(-1, 1).to(device)
        real_features = torch.tensor(self.rs[idx], dtype=torch.float32).to(device)
        mask = torch.tensor(self.mask[idx], dtype=torch.float32).to(device)

        return s, s_, a, r, done, real_features, mask

    def store(self, s, s_, real_a, r, done, rs, mask):
        if self.length == self.capacity:
            self.resize()
        self.s[self.length, :] = s
        self.s_[self.length, :] = s_
        self.reala[self.length, :] = real_a
        self.r[self.length, :] = r
        self.done[self.length, :] = done
        self.rs[self.length, :] = rs
        self.mask[self.length, :] = mask
        self.length += 1

    def resize(self):
        # Grow capacity
        new_capacity = self.capacity * 2
        self.s = np.resize(self.s, (new_capacity, self.feature_dim * 34))
        self.s_ = np.resize(self.s_, (new_capacity, self.feature_dim * 34))
        self.reala = np.resize(self.reala, (new_capacity, 1))
        self.r = np.resize(self.r, (new_capacity, 1))
        self.done = np.resize(self.done, (new_capacity, 1))
        self.rs = np.resize(self.rs, (new_capacity, self.feature_dim))
        self.mask = np.resize(self.mask, (new_capacity, 34))
        self.capacity = new_capacity

    def reset_buffer(self):
        self.length = 0

    def list_to_tensor(self, device):
        # Slice to current length and convert to tensors; also build rns (next real features) for TD targets
        s = torch.tensor(self.s[0:self.length, :], dtype=torch.float).to(device)
        s_ = torch.tensor(self.s_[0:self.length, :], dtype=torch.float).to(device)
        reala = torch.tensor(self.reala[0:self.length, :], dtype=torch.int64).view(-1, 1).to(device)
        r = torch.tensor(self.r[0:self.length, :], dtype=torch.float).view(-1, 1).to(device)
        done = torch.tensor(self.done[0:self.length, :], dtype=torch.int64).view(-1, 1).to(device)
        rs = torch.tensor(self.rs[0:self.length, :], dtype=torch.float).to(device)
        mask = torch.tensor(self.mask[0:self.length, :], dtype=torch.int64).to(device)
        rns = rs[1:]
        rns = torch.vstack((rns, rs[-1:]))
        return s, s_, reala, r, done, rs, rns, mask
