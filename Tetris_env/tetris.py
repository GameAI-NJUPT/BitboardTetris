import copy
from concurrent.futures.thread import ThreadPoolExecutor
import os
import jpype
import numpy as np


class Tetris:
    # Bridge to the Java Tetris engine via JPype; exposes transition and feature interfaces to Python
    def __init__(self):
        current_file_path = os.path.abspath(__file__)
        current_dir = os.path.dirname(current_file_path)
        # Path to JVM
        jvm_path = current_dir + r"\openjdk-21.0.1\bin\server\jvm.dll"
        # Java classpath (compiled classes)
        class_path = current_dir + r"\TetrisSelf\out\production\TetrisTheta"

        # Start JVM if needed
        if not jpype.isJVMStarted():
            # Initialize JPype with classpath
            jpype.startJVM(jvm_path, "-Djava.class.path=" + class_path)
        TetrisDTFeature = jpype.JClass("tetris.TetrisDTFeature")
        ValueFunction = jpype.JClass("rl.ValueFunction")
        self.TetrisEnvironment = jpype.JClass("tetris.TetrisEnvironment")
        self.Blocks = jpype.JClass("tetris.Blocks")
        self.TreeNodeState = jpype.JClass("search.TreeNodeState")
        PlayerExpectimax = jpype.JClass("search.PlayerExpectimax")
        self.ParallelismTest = jpype.JClass("rl.ParallelismTest")
        self.TetrisDTFeature = jpype.JClass("tetris.TetrisDTFeature")
        # Player without search
        Player = jpype.JClass("player.Player")
        # Instantiate feature extractors and value function
        c = TetrisDTFeature()
        n = TetrisDTFeature()
        # Number of features
        feature_count = c.getNumberoffeature()
        self.vf = ValueFunction(c, n, feature_count)
        self.player = Player(self.vf)
        # Search parameters
        self.pe = PlayerExpectimax(self.vf.copy(), 0.15)

    def reset(self):
        return self.TetrisEnvironment.defaultInitialState()

    def isFinal(self, state):
        return self.TetrisEnvironment.isFinal(state)

    def step(self, currentState, action):
        nextState = self.TetrisEnvironment.successorState(currentState, action)
        return nextState, nextState[10], self.isFinal(nextState)

    def parallel_episode(self):
        p = self.ParallelismTest(self.player)
        p.go()
        return p.getNum()

    # Compute 306 features (34*9) and mask from a 15-d state
    def get_9featureAndMask(self, currentState):
        # Returns: 34*9 afterstate feature vector and length-34 valid action mask (1 valid, 0 invalid)
        featureAndMask = self.vf.get9featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:306])
        mask = np.array(featureAndMask[306:340], dtype=int)
        return featureArray, mask

    def get_260feature(self, currentState):
        featureAndMask = self.vf.get260featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:8840])
        mask = np.array(featureAndMask[8840:8874])
        return featureArray, mask

    def get_252feature(self, currentState):
        featureAndMask = self.vf.get252featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:8568])
        mask = np.array(featureAndMask[8568:8602])
        return featureArray, mask

    def get_231feature(self, currentState):
        featureAndMask = self.vf.get231featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:7854])
        mask = np.array(featureAndMask[7854:7888])
        return featureArray, mask

    def get_205feature(self, currentState):
        featureAndMask = self.vf.get205featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:6970])
        mask = np.array(featureAndMask[6970:7004])
        return featureArray, mask

    def get_48featureAndMask(self, currentState):
        featureAndMask = self.vf.get48featureAndMask(currentState)
        featureArray = np.array(featureAndMask[0:1632])
        mask = np.array(featureAndMask[1632:1666])
        return featureArray, mask

    def get_2orderfeature(self, currentState):
        featureAndMask = self.vf.get9featureAndMask_2order(currentState)
        featureArray = np.array(featureAndMask[0:1836])
        mask = np.array(featureAndMask[1836:1870])
        return featureArray, mask

    def getMask(self, currentState):
        actionList = self.TetrisEnvironment.getActionList(currentState)
        mask = np.zeros(34)
        actionSize = self.TetrisEnvironment.blocks.squares[currentState[12]].actionSize
        for i in range(actionSize):
            if self.TetrisEnvironment.actionValid(i, actionList):
                mask[i] = 1
        return mask.reshape(1, -1)

    def episode(self, testEpisode, agent):
        def run_single_episode(agent):
            currentState = self.reset()  # Assume independent environment per episode
            for _ in range(int(2e10)):
                feature, mask = self.get_9featureAndMask(currentState)
                action = agent.choose_max_action(feature, mask)
                nextState, reward, done = self.step(currentState, action)
                if done:
                    break
                currentState = nextState
            return currentState[11]

        total_reward = 0
        for episode_num in range(testEpisode):
            total_reward += run_single_episode(agent)
        return total_reward / testEpisode
