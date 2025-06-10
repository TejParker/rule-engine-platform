/**
 * {"A_31":"25061003092","A_32":"25061003093","finalStatusColor":"#03BF62","A_30":"25061003091","client_id":"87c613068cf34b8ea79c335ac8c547c6","create_date_number":1749542465626,"dev_template_unique_id":"L6M7NKg76R42pU9UUgi5","iot_unique_message_id":"f0ab15753575a8ddfdad269a11aee181","A_28":"1","id":"f0ab15753575a8ddfdad269a11aee181","A_29":"25061003095","A_26":"1","create_date":"2025-06-10 16:01:05","A_27":"1","A_24":"1","A_25":"0","_col_counts":"{\"2025-06-10 16:01:04\":{\"FinishPartsNum\":\"1779\"},\"2025-06-10 16:01:02\":{\"FinishPartsNum\":\"1779\"},\"2025-06-10 16:01:03\":{\"FinishPartsNum\":\"1779\"},\"2025-06-10 16:01:00\":{\"FinishPartsNum\":\"1779\"},\"2025-06-10 16:01:01\":{\"FinishPartsNum\":\"1779\"}}","Status":"1","finalStatusName":"加工","device_id":"57224","that_moment_program_version_id":"-1","CncStatus":"1","A_1":"1779","A_2":"0","A_3":"0","A_4":"238","pbox_id":"863812076657901","A_5":"356","A_6":"0.268","A_7":"360","A_8":"0","A_9":"0.34","sendtime":null,"A_11":"355","A_12":"0.359","A_10":"357","statusColor":"#03BF62","transformed_date":"2025-06-10 16:01:05","FinishPartsNum":"1779","statusName":"加工","NoChange":"0","data_col_time":"2025-06-10 16:01:04","A_22":"0","A_23":"0","A_20":"0","ConnectStatus":"1","A_21":"0","finalCncStatus":"1","receive_date":"2025-06-10 16:01:05","A_19":"0","A_17":"0","A_18":"0","A_15":"0","A_16":"0","A_13":"3","A_14":"0.83"}
 */

package top.doe.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoJson {
    public static void main(String[] args) {
        String jsonStr = "{\"A_31\":\"25061003092\",\"A_32\":\"25061003093\",\"finalStatusColor\":\"#03BF62\",\"A_30\":\"25061003091\",\"client_id\":\"87c613068cf34b8ea79c335ac8c547c6\",\"create_date_number\":1749542465626,\"dev_template_unique_id\":\"L6M7NKg76R42pU9UUgi5\",\"iot_unique_message_id\":\"f0ab15753575a8ddfdad269a11aee181\",\"A_28\":\"1\",\"id\":\"f0ab15753575a8ddfdad269a11aee181\",\"A_29\":\"25061003095\",\"A_26\":\"1\",\"create_date\":\"2025-06-10 16:01:05\",\"A_27\":\"1\",\"A_24\":\"1\",\"A_25\":\"0\",\"_col_counts\":\"{\\\"2025-06-10 16:01:04\\\":{\\\"FinishPartsNum\\\":\\\"1779\\\"},\\\"2025-06-10 16:01:02\\\":{\\\"FinishPartsNum\\\":\\\"1779\\\"},\\\"2025-06-10 16:01:03\\\":{\\\"FinishPartsNum\\\":\\\"1779\\\"},\\\"2025-06-10 16:01:00\\\":{\\\"FinishPartsNum\\\":\\\"1779\\\"},\\\"2025-06-10 16:01:01\\\":{\\\"FinishPartsNum\\\":\\\"1779\\\"}}\",\"Status\":\"1\",\"finalStatusName\":\"加工\",\"device_id\":\"57224\",\"that_moment_program_version_id\":\"-1\",\"CncStatus\":\"1\",\"A_1\":\"1779\",\"A_2\":\"0\",\"A_3\":\"0\",\"A_4\":\"238\",\"pbox_id\":\"863812076657901\",\"A_5\":\"356\",\"A_6\":\"0.268\",\"A_7\":\"360\",\"A_8\":\"0\",\"A_9\":\"0.34\",\"sendtime\":null,\"A_11\":\"355\",\"A_12\":\"0.359\",\"A_10\":\"357\",\"statusColor\":\"#03BF62\",\"transformed_date\":\"2025-06-10 16:01:05\",\"FinishPartsNum\":\"1779\",\"statusName\":\"加工\",\"NoChange\":\"0\",\"data_col_time\":\"2025-06-10 16:01:04\",\"A_22\":\"0\",\"A_23\":\"0\",\"A_20\":\"0\",\"ConnectStatus\":\"1\",\"A_21\":\"0\",\"finalCncStatus\":\"1\",\"receive_date\":\"2025-06-10 16:01:05\",\"A_19\":\"0\",\"A_17\":\"0\",\"A_18\":\"0\",\"A_15\":\"0\",\"A_16\":\"0\",\"A_13\":\"3\",\"A_14\":\"0.83\"}";

        // 将json字符串解析为JSONObject对象
        JSONObject jsonObject = JSON.parseObject(jsonStr);

        // 获取指定字段的值
        String deviceId = jsonObject.getString("device_id");
        String finishPartsNum = jsonObject.getString("FinishPartsNum"); 
        String statusName = jsonObject.getString("statusName");
        String noChange = jsonObject.getString("NoChange");
        String A_29 = jsonObject.getString("A_29");

        log.info("设备ID: {}", deviceId);
        log.info("完成零件数: {}", finishPartsNum);
        log.info("状态名称: {}", statusName);
        log.info("NoChange: {}", noChange);
        log.info("A_29: {}", A_29);
    }
}