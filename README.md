# Bitboard version of Tetris AI

## Abstract
- The system adopts **Bitboard** as the core state representation and introduces **afterstate feature modeling**.
- Compared the **REINFORCE** algorithm (based on action-value function Q and afterstate), **Trajectory PPO** and **Buffer PPO** algorithms on a **34-dimensional action space**.
- Bridged **Python and Java engine** via **JPype**, which retains the high efficiency of Java engine in bitwise operations and rule simulation, while enabling convenient **deep reinforcement learning** training in Python.

### System Architecture
- Python Side (**Training**)
  - Policy/value function networks and training loops (**PyTorch**)
  - Comparative training of three algorithms: **REINFORCE**, **Trajectory PPO** and **Buffer PPO**
  - Sampling, experience buffer, logging and model persistence
- Java Side (**Engine**)
  - **Bitboard-based** Tetris state representation and state transition for underlying acceleration
  - Feature extraction (**9 dimensions/action**) and legal action masking
- Cross-language Communication
  - **JPype** launches JVM and invokes Java classes, see **[tetris.py]**

## Method Overview
- Action Space: **34 discrete actions** (combinations of rotation/translation/drop, etc.)
- Feature Representation:
  - Afterstate features: **9 dimensions per action**, totaling **34×9 inputs** to the policy network
  - Value function input: **9-dimensional afterstate features** (corresponding to the actually executed action)
- Policy Network and Masking:
  - A shared linear layer is applied to the 9-dimensional features of each action, outputs log values which are combined with masks (illegal action positions are masked to -inf), then softmax is applied to obtain the action distribution
  - See **[PolicyNet_Afterstates.forward]**
- PPO Update:
  - **Generalized Advantage Estimation (GAE)** is used to estimate advantages, with **Clip surrogate objective** adopted; **Mean Squared Error (MSE)** regression on TD target is used for the value function
  - Key implementations are in **[ppo_train.update]** and Buffer version **[PPO_discrete.update]**

## Directory Structure
- **Tetris_env**
  - **tetris.py**: Python environment wrapper (invokes Java engine via JPype, provides interfaces for 9-dimensional features and masking)
  - **openjdk-21.0.1**: Bundled Windows JDK
  - **TetrisSelf**: Java source code and build artifacts for engine and feature extraction
- **Tetris_AI**
  - algorithms
    - REINFORCE
      - **REINFORCE_Q.py**: REINFORCE based on action-value function Q
      - **REINFORCE_afterstate.py**: REINFORCE based on afterstate
    - PPO/**ppo_train.py**: Trajectory PPO training script (temperature annealing, logging and model saving)
    - PPO_Buffer: PPO version that collects and updates samples by fixed batch
      - **train.py**: Sampling and batch update, parallel evaluation
      - **ppo_discrete.py**: Discrete-action PPO + training tricks (Trick 1–10)
      - **replaybuffer.py**: Batch buffer (storing s/s_/a/a_logprob/r/done/rs/mask)
  - common
    - **Net.py**: Policy/value function networks (support masking, some versions support temperature scaling)
    - **ReplayBuffer.py**: Full-episode trajectory buffer (used for single-episode PPO)
  - utils
    - **rl_utils.py**: Utility functions such as advantage function calculation

## Implementation Details
- Policy/Value Functions
  - Policy: Performs action-wise linear mapping on **[34, 9]** feature tensor followed by masked softmax (see **[Net.py]**)
  - Value Function: Takes **9-dimensional afterstate features** as input and outputs scalar V (see **[ValueNet]**)
- Training Tricks (**PPO_Buffer**)
  - Configurable tricks including advantage normalization, entropy regularization, learning rate decay, gradient clipping, orthogonal initialization, Adam epsilon, etc. (see **[ppo_discrete.PPO_discrete.__init__]**)
- Sampling and Update
  - **Buffer PPO**: Collects samples by batch_size, then performs K iterations with mini-batches (see **[PPO_Buffer/train.py]**)

## Environment and Dependencies
- Please first **download OpenJDK 21.0.1** and extract it into the **[Tetris_env]** directory.
- Windows JDK: Uses jvm.dll from **[Tetris_env/openjdk-21.0.1]** by default; modify the path in **[tetris.py]** for custom JDK
- **Python 3.10.19**, recommended installations:
  - ```pip install -r requirements.txt```

## Quick Start
- Train **REINFORCE (afterstate)**
   - ```python Tetris_AI/algorithms/REINFORCE/REINFORCE_afterstate/REINFORCE_afterstate.py```
- Train **PPO**
   - ```python Tetris_AI/algorithms/PPO/ppo_train.py```
- Train **PPO (Buffer Version)**
   - ```python Tetris_AI/algorithms/PPO_Buffer/train.py```
- Modify parameters such as the number of board rows in **[Tetris_env/TetrisSelf/TetrisTheta/src/settings/Parameters.java]** and recompile the Java code if needed.