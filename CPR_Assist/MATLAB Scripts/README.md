# MATLAB Scripts for CPR Assist Glove

## Overview
This folder contains MATLAB scripts to calibrate a DIY Velostat pressure sensor used in the CPR Assist Glove project. The scripts help users establish the relationship between analog readings from the sensor and corresponding weights using a logarithmic curve fit.

## Setup
1. Ensure MATLAB is installed on your computer.
2. Clone the repository to your local machine.
3. Navigate to the MATLAB folder within the cloned repository.

## Usage
1. Open MATLAB.
2. Load the provided script file `Weight_Analog Read Curve.m`.
3. **Input Your Data:**
   - Modify the `x` and `y` arrays in the script with your measured data points.
   - Replace `x` with analog readings (e.g., `[1023, 990, 975, ...]`).
   - Replace `y` with corresponding weights (e.g., `[0.01, 0.05, 0.1, ...]`).
     
4. Run the script to process your data and fit the logarithmic curve.

## Viewing Results
- The script will output the coefficients of the logarithmic equation that describes the relationship between analog readings and weights.
- It will also plot the measured data points along with the fitted calibration curve for visual analysis.

## Example
Suppose your data points are:
```matlab
% Given data points
x = [1023, 990, 975, 760, 630, 500, 330, 300, 250, 200, 180, 170];
y = [0.01, 0.05, 0.1, 2, 4.5, 6.5, 8.5, 11, 13, 15.5, 18.5, 21];
```

Modify `Weight_Analog Read Curve.m`:
```matlab
% Given data points
x = [your_analog_readings];  % Replace with your analog readings
y = [your_corresponding_weights];  % Replace with your corresponding weights
```

## Notes
- Ensure your data files are formatted correctly with columns for analog readings and corresponding weights.
- Modify the script as needed to accommodate different data formats or additional analysis requirements.
