# Terminal Radar
This is a light weight visualizer of air traffic in your location for dump1090 messages.
## Prerequisites
- rtl-sdr module with antenna connected to your device and its drivers installed

  <img src="scheme.png" width="315"/>

- Java SDK installed
- Dump1090 installed and running (tcp dump on port 30003)

  [Useful scripts](https://github.com/ThreadFool/satelite-fuck-around-and-find-out/tree/main/scripts)

## How to run

Build the project:
```bash
./gradlew clean build
```

Export you location coordinates:
```bash
export MY_LAT=50.000
export MY_LONG=20.000
```

Run the app:
```bash
./run.sh
```

## This is what you should be able to see:
[<img src="radar_view.png" width="500"/>](image.png)

## Testing 

If you would like to play around with code you can test the app using mocked dump1090 tcp server in test resources
