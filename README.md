# XO-Game-AI
Tic-Tac-Toe AI with Alpha–Beta Pruning  
Classical Heuristic & Machine Learning Evaluation

---

## Overview
This project implements an intelligent Tic-Tac-Toe AI using Alpha–Beta pruning.

The AI supports two evaluation modes:
- Classical hand-crafted heuristic
- Machine-learned evaluation model

The user can choose:
- Player symbol (X or O)
- Difficulty level (Easy, Normal, Hard)
- Evaluation function (Classical or Machine Learning)

This project was developed as part of an Artificial Intelligence course.

---

## Project Objectives
- Implement a complete 3×3 Tic-Tac-Toe game environment
- Apply Alpha–Beta pruning for decision making
- Compare classical heuristic evaluation with machine learning evaluation
- Support multiple difficulty levels
- Enable user interaction through a console-based interface

---

## AI Techniques Used

### Alpha–Beta Pruning
- Efficient search over the game tree
- Prunes unnecessary branches
- Uses a depth limit based on difficulty:
  - Easy: Low depth
  - Normal: Medium depth
  - Hard: Maximum depth (near-optimal play)

---

## Evaluation Functions

### Classical Heuristic Evaluation
A hand-crafted evaluation function based on:
- Number of X marks
- Number of O marks
- Near-winning rows, columns, and diagonals
- Control of center and corners

This function provides fast and interpretable evaluations.

---

### Machine Learning Evaluation
A neural network (Multi-Layer Perceptron) trained on a dataset of Tic-Tac-Toe game states.

Input features (6):
- Number of X marks
- Number of O marks
- X almost winning lines
- O almost winning lines
- X in center
- X in corners

Output:
- +1 indicates X eventually wins
- −1 indicates O eventually wins

The trained model predicts a score used by the Alpha–Beta search:

score = P(X wins) − P(O wins)

## Project Structure
XO-Game-AI/
│
├── TicTacToeAI.java Main game logic and Alpha–Beta search
├── MLModel.java Trained neural network (inference only)
├── MLPTrainer.java Neural network training code
├── tictactoe_dataset.csv Dataset used for training
├── .gitignore
└── README.md

---

## Machine Learning Training
- Network architecture: 6 → 18 → 8 → 2
- Activation function: Leaky ReLU
- Optimizer: Gradient Descent
- Loss function: Cross-Entropy
- Dataset split:
  - 70% Training
  - 15% Validation
  - 15% Testing

After training, learned weights are exported and embedded into `MLModel.java`.

---

## User Interaction
The game allows the user to:
- Choose the player symbol
- Select the difficulty level
- Select the evaluation function
- View the board after each move
- Observe AI evaluation scores for all possible moves

---

## How to Run

Clone the repository:
```bash
git clone https://github.com/yamanaboasal2/XO-Game-AI.git

Compile and run:
javac TicTacToeAI.java
java TicTacToeAI

Optional: Train the machine learning model:
javac MLPTrainer.java
java MLPTrainer

Key Learning Outcomes

Alpha–Beta pruning optimization

Design of heuristic evaluation functions

Integration of machine learning models with classical search

Comparison of symbolic and data-driven AI approaches

Feature engineering for game state representation

Author

Yaman Abo Asal
4th Year Computer Engineering Student
Artificial Intelligence Course Project

Notes

Hard difficulty with machine learning evaluation results in near-optimal gameplay

Classical heuristic evaluation provides explainable decisions

The machine learning model generalizes well on unseen board states
