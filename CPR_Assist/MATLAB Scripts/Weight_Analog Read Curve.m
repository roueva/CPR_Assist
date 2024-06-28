% Given data points
x = [1023, 990, 975, 760, 630, 500, 330, 300, 250, 200, 180, 170];
y = [0.01, 0.05, 0.1, 2, 4.5, 6.5, 8.5, 11, 13, 15.5, 18.5, 21];

% Plot the data points
scatter(x, y, 'bo', 'filled', 'DisplayName', 'Measured Data');
hold on;

% Fit a logarithmic curve to the data
coefficients = polyfit(log(x), y, 1);

% Print the coefficients of the logarithmic equation
disp('Logarithmic Equation:');
disp(['Weight = ', num2str(coefficients(1)), ' * log(Analog Reading) + ', num2str(coefficients(2))]);

% Plot the fitted curve
x_fit = linspace(min(x), max(x), 100);
y_fit = coefficients(1) * log(x_fit) + coefficients(2);
plot(x_fit, y_fit, 'r-', 'LineWidth', 1.5, 'DisplayName', 'Calibration Curve');
hold off;

xlabel('Analog Reading of Pressure Sensor');
ylabel('Corresponding Weight');
title('Pressure Sensor Reading vs Weight');
legend('Location', 'northeast');
grid on;
