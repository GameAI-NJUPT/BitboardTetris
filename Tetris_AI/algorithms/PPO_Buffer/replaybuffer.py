import torch
import numpy as np


class ReplayBuffer:
    # Batch trajectory buffer: collect samples up to batch_size, then trigger one PPO update
    def __init__(self, args):
        self.s = np.zeros((args.batch_size, 306))
        self.s_ = np.zeros((args.batch_size, 306))
        self.a = np.zeros((args.batch_size, 1))          # taken action
        self.a_logprob = np.zeros((args.batch_size, 1))  # log-prob of taken action
        self.r = np.zeros((args.batch_size, 1))          # immediate reward
        self.done = np.zeros((args.batch_size, 1))       # terminal flag
        self.rs = np.zeros((args.batch_size, 9))         # 9-d afterstate features of the taken action
        self.mask = np.zeros((args.batch_size, 34))      # action mask
        self.count = 0

    def store(self, s, s_, a, a_logprob, r, done, rs, mask):
        # Append one sample
        self.s[self.count] = s
        self.s_[self.count] = s_
        self.a[self.count] = a
        self.a_logprob[self.count] = a_logprob
        self.r[self.count] = r
        self.done[self.count] = done
        self.rs[self.count] = rs
        self.mask[self.count] = mask
        self.count += 1

    def numpy_to_tensor(self):
        # Convert to tensors for batch training
        s = torch.tensor(self.s, dtype=torch.float)
        s_ = torch.tensor(self.s_, dtype=torch.float)
        a = torch.tensor(self.a, dtype=torch.long)
        a_logprob = torch.tensor(self.a_logprob, dtype=torch.float)
        r = torch.tensor(self.r, dtype=torch.float)
        done = torch.tensor(self.done, dtype=torch.int64)
        rs = torch.tensor(self.rs, dtype=torch.float)
        mask = torch.tensor(self.mask, dtype=torch.int64)
        return s, s_, a, a_logprob, r, done, rs, mask
