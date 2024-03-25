# ECG Simulator Plus Widget

## Notes

Changes and additions of original project:
1) The CardView in which the widget is included has a name.
2) When deleting cardviw/widget, you must confirm the deletion.
3) Cardview is supplemented with a simulation option: can turn the ECG simulator on and off using the switch button at the bottom left of the control.
4) The simulator is implemented in the ECGSimulator class. It has a round (circular) buffer designed for several data series: this value can be adjusted.
5) The simulation itself is performed in the HandlerThreadWrapper class: with an interval of 1s, an ECG signal of a given length is written to the circular buffer of the sensor.
6) Next, an independently working widget read and transfers this data in portions to its own circular buffer and visualizes the signal. All operations are performed asynchronously.

## Movie

https://github.com/mk590901/ECG-Simulator-Plus-Widget/assets/125393245/979890e7-2032-4563-b7f7-a935eaf62e74


