package cn.hitftcl.wearablepc.Utils.ModelOperation;


import cn.hitftcl.wearablepc.Model.EnviromentTable;

public class EnviromentTableOperation {
    public static boolean SaveEnviromentTable(double temperature, double pressure, double humidity, double SO2, double NO, double voltage){
        EnviromentTable enviromentTable = new EnviromentTable(temperature,pressure,humidity,SO2,NO,voltage);
        return  enviromentTable.save();

    }
}
