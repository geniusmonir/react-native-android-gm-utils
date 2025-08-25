import {
  isBatteryOptimizationEnabled,
  requestDisableBatteryOptimization,
} from 'react-native-android-gm-utils';
import { View, StyleSheet, Button } from 'react-native';

export default function App() {
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
