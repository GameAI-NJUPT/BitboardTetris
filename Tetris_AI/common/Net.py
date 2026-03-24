import torch
import torch.nn as nn
import torch.nn.functional as F


class PolicyNet_Afterstates(torch.nn.Module):
    # Input: 9-d afterstate features per action (34 actions); Output: 34-action probability distribution
    def __init__(self):
        super(PolicyNet_Afterstates, self).__init__()
        self.linear_layer = nn.Linear(9, 1)

    def forward(self, x, mask, temperature):
        # x: [B, 34*9] -> [B, 34, 9]; mask: [B, 34, 1]; invalid actions masked to -inf before softmax
        x_reshaped = x.reshape(x.shape[0], 34, 9)
        mask = mask.view(x.shape[0], 34, 1)
        outputs = self.linear_layer(x_reshaped)
        outputs = outputs.masked_fill(mask == 0, float('-inf'))
        outputs = outputs.squeeze(dim=2)
        outputs = outputs / temperature
        return F.softmax(outputs, dim=1)


class PolicyNet_Qsa(torch.nn.Module):
    # Q(s, a)-style variant with 48-d action features
    def __init__(self):
        super(PolicyNet_Qsa, self).__init__()
        self.linear_layer = nn.Linear(48, 1)

    def forward(self, x, mask, temperature):
        x_reshaped = x.reshape(x.shape[0], 34, 48)
        mask = mask.view(x.shape[0], 34, 1)
        outputs = self.linear_layer(x_reshaped)
        outputs = outputs.masked_fill(mask == 0, float('-inf'))
        outputs = outputs.squeeze(dim=2)
        outputs = outputs / temperature
        return F.softmax(outputs, dim=1)


class PolicyNet_Actions(torch.nn.Module):
    # MLP variant that outputs action distribution from a compact state summary
    def __init__(self):
        super(PolicyNet_Actions, self).__init__()
        self.fc1 = nn.Linear(10, 128)
        self.fc2 = nn.Linear(128, 128)
        self.fc3 = nn.Linear(128, 34)

    def forward(self, x, mask, temperature):
        mask = mask.view(x.shape[0], 34)
        outputs = F.relu(self.fc1(x))
        outputs = F.relu(self.fc2(outputs))
        outputs = self.fc3(outputs)
        outputs = outputs.masked_fill(mask == 0, float('-inf'))
        outputs = outputs / temperature
        return F.softmax(outputs, dim=1)


class PolicyNet2(torch.nn.Module):
    # Generic two-layer policy with configurable input dimension
    def __init__(self, input_dim):
        super(PolicyNet2, self).__init__()
        self.fc1 = nn.Linear(input_dim, 256)
        self.fc2 = nn.Linear(256, 1)
        self.input_dim = input_dim
        self.dSilu = dSiLU()

    def forward(self, x, mask, temperature):
        x_reshaped = x.reshape(x.shape[0], 34, self.input_dim)
        mask = mask.view(x.shape[0], 34, 1)
        outputs = self.dSilu(self.fc1(x_reshaped))
        outputs = self.fc2(outputs)
        outputs = outputs.masked_fill(mask == 0, float('-inf'))
        outputs = outputs.squeeze(dim=2)
        outputs = outputs / temperature
        return F.softmax(outputs, dim=1)


class ValueNet(torch.nn.Module):
    # Value function: input 9-d afterstate features, output scalar V
    def __init__(self, input_dim):
        super(ValueNet, self).__init__()
        self.fc1 = torch.nn.Linear(input_dim, 1)

    def forward(self, x):
        x = self.fc1(x)
        return x


class ValueNet2(torch.nn.Module):
    def __init__(self, input_dim):
        super(ValueNet2, self).__init__()
        self.fc1 = nn.Linear(input_dim, 250)
        self.fc2 = nn.Linear(250, 1)
        self.dSilu = dSiLU()

    def forward(self, x):
        x = self.dSilu(self.fc1(x))
        return self.fc2(x)


class dSiLU(nn.Module):
    def forward(self, x):
        sigmoid_x = torch.sigmoid(x)
        return sigmoid_x * (1 + x * (1 - sigmoid_x))
