import {
  getNotificationPermissionStatus,
  isBatteryOptimizationEnabled,
  requestDisableBatteryOptimization,
} from 'react-native-android-gm-utils';
import { View, StyleSheet, Button } from 'react-native';
import { useEffect } from 'react';

export default function App() {
  useEffect(() => {
    const getStatus = async () => {
      const status = await getNotificationPermissionStatus();
      console.log('The status is: ', status);
    };
    getStatus();
  }, []);

  return (
    <View style={styles.container}>
      <Button
        title="Request Permission"
        onPress={async () => {
          console.log(await isBatteryOptimizationEnabled());
          requestDisableBatteryOptimization();
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
