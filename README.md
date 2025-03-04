# Welcome to the DroneHub Project!

This repository is for the Android application that connects the DJI drones and the DroneHub server through an Android device (DJI smartcontroller or mobile phone).
This app is based on DJI's Mobile SDK V4 and therefore are only compatible with the devices listed here: https://developer.dji.com/products/#!/mobile

Follow the steps below to set up the project on your local machine.

## Prerequisites
Before you begin, ensure you have the following installed:
- [Android Studio](https://developer.android.com/studio)
- Java Development Kit (JDK) 11 or higher
- Gradle (bundled with Android Studio)

## Setting Up the API Key
This project requires a DJI API key for proper functionality. Follow these steps to set it up:

1. **Create an API key** in https://developer.dji.com.
3. **Add your API key** to `AndroidManifest.xml`:
   ```properties
   <meta-data
            android:name="com.dji.sdk.API_KEY"
            android:value="INSERT API KEY" />
   ```

## Running the Project
1. Open the project in Android Studio.
2. Sync Gradle by clicking "Sync Now" when prompted.
3. Run the app on an emulator or a connected device.

## Troubleshooting
- If you encounter issues with the API key not being recognized, ensure that:
  - `local.properties` contains `API_KEY=your_api_key_here`
  - The Gradle sync completes successfully
- If Gradle fails, try running:
  ```sh
  ./gradlew clean
  ./gradlew build
  ```

## Contributing
Feel free to open issues or submit pull requests to improve this setup.

## License
This project is licensed under the MIT License. See `LICENSE` for details.

