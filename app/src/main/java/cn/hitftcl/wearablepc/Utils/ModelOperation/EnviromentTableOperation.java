package cn.hitftcl.wearablepc.Utils.ModelOperation;


import cn.hitftcl.wearablepc.Model.EnvironmentTable;

public class EnviromentTableOperation {
    public static boolean SaveEnviromentTable(double temperature, double pressure, double humidity, double SO2, double NO, double voltage,String IP){
        EnvironmentTable environmentTable = new EnvironmentTable(temperature,pressure,humidity,SO2,NO,voltage,IP);
        return  environmentTable.save();

    }
}
