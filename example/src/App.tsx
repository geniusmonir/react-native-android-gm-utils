import {
  multiply,
  getNotificationPermissionStatus,
  requestNotificationPermission,
  addNumbers,
} from 'react-native-android-gm-utils';
import { Text, View, StyleSheet, Button } from 'react-native';
import { useState, useEffect } from 'react';

export default function App() {
  const [result, setResult] = useState<number | undefined>();
  const [resultA, setResultA] = useState<number | undefined>();

  useEffect(() => {
    multiply(3, 7).then(setResult);

    addNumbers(3, 7).then(setResultA);

    const getStatus = async () => {
      const status = await getNotificationPermissionStatus();
      console.log('The status is: ', status);
    };
    getStatus();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Text>Result A: {resultA}</Text>

      <Button
        title="Request Permission"
        onPress={async () => {
          await requestNotificationPermission();
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
