package telran.monitoring;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.monitoring.api.JumpPulseData;
import telran.monitoring.api.LatestValueSaver;
import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

public class App {
  private static final String DEFAULT_FACTOR = "0.7f";
  private static final String DEFAULT_MESSAGE_BOX_CLASS = "telran.monitoring.JumpPulseDataMessageBox";
  private static final String DEFAULT_MESSAGE_BOX = "jump_pulse_values";

  Logger logger = new LoggerStandard("jump-pulse-recognizer");
  LatestValueSaver lastValues = new LatestValuesSeverMap();
  Map<String, String> env = System.getenv();

  public void handleRequest(final DynamodbEvent event, final Context context) {
    event.getRecords().forEach(r -> {
      Map<String, AttributeValue> map = r.getDynamodb().getNewImage();
      long patientId = Long.parseLong(map.get("patientId").getN());
      int value = Integer.parseInt(map.get("value").getN());
      long timestamp = Long.parseLong(map.get("timestamp").getN());
      SensorData sensorData = new SensorData(patientId, value, timestamp);
      logger.log("finest", sensorData.toString());
      computeSensorData(sensorData);
    });
  }

  private void computeSensorData(SensorData sensorData) {
    long patientId = sensorData.patientId();
    int currentValue = sensorData.value();
    int lastValue = 0;
    SensorData lastSensorData = lastValues.getLastValue(patientId);
    if (lastSensorData != null) {
      lastValue = lastSensorData.value();
    }
    if (currentValue != lastValue) {
      lastValues.addValue(sensorData);
      logger.log("info", "Current and last values are different (%d, %d)".formatted(lastValue, currentValue));
      recognizePulseJump(sensorData, lastValue);
    }
  }

  private void recognizePulseJump(SensorData sensorData, int lastValue) {
    int currentValue = sensorData.value();
    float factor = Float.parseFloat(getFactor());
    if (lastValue != 0 && Math.abs(lastValue - currentValue) / (float) lastValue >= factor) {
      logger.log("info", "Pulse jump has recognized form %d to %d".formatted(lastValue, currentValue));
      JumpPulseData jumpPulseData = new JumpPulseData(sensorData.patientId(), lastValue, currentValue,
          System.currentTimeMillis());
      saveJumpPulseData(jumpPulseData);
    }
  }

  @SuppressWarnings("unchecked")
  private void saveJumpPulseData(JumpPulseData jumpPulseData) {
    BasicConfigurator.configure();
    try {
      MessageBox<JumpPulseData> messageBox = MessageBoxFactory.getMessageBox(getMessageBoxClass(), getMessageBox());
      messageBox.put(jumpPulseData);
    } catch (Exception e) {
      logger.log("error", e.toString());
    }
  }

  private String getFactor() {
    return env.getOrDefault("FACTOR", DEFAULT_FACTOR);
  }

  private String getMessageBoxClass() {
    return env.getOrDefault("MESSAGE_BOX_CLASS", DEFAULT_MESSAGE_BOX_CLASS);
  }

  private String getMessageBox() {
    return env.getOrDefault("MESSAGE_BOX_NAME", DEFAULT_MESSAGE_BOX);
  }
}
