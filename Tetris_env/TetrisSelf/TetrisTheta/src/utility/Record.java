package utility;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Record {

    int recordNum;
    double[] record;


    public Record(int iteration) {
        this.recordNum = iteration;
        record = new double[this.recordNum];
    }

    public void setResult(double rms, int i) {
        this.record[i] = rms;
    }

    public double getResult(int i) {
        return this.record[i];
    }

    public double averaged() {
        double sum = 0;
        for (int i = 0; i < this.recordNum; i++) {
            sum += this.record[i];
        }
        return sum / this.recordNum;
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < this.recordNum; i++) {
            s += this.record[i] + ",";
        }

        return s;
    }

    public Record copy() {
        Record r = new Record(recordNum);
        for (int i = 0; i < this.recordNum; i++) {
            r.record[i] = this.record[i];
        }
        return r;
    }

    public void writeResultToUTF(String s) {
        FileOutputStream fws = null;
        DataOutputStream out = null;
        try {
            fws = new FileOutputStream(s);
            out = new DataOutputStream(fws);


            String resultString = "";
            int lineNum = 100;
            for (int i = 0; i < this.record.length / lineNum + 1; i++) {
                resultString = "";
                for (int j = 0; j < lineNum; j++) {
                    if (i * lineNum + j >= this.record.length) {
                        break;
                    }
                    resultString += this.record[i * lineNum + j] + " ";
                }
                resultString += "\n";
                out.write(resultString.getBytes("utf8"));
                out.flush();
            }
            System.out.println("save to file: " + s);

        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + s);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fws != null) fws.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    public void writeStringToUTF(String content, String fileName) {
        FileOutputStream fws = null;
        DataOutputStream out = null;
        try {
            fws = new FileOutputStream(fileName);
            out = new DataOutputStream(fws);


            out.write(content.getBytes("utf8"));
            out.flush();
        } catch (FileNotFoundException ex) {
            System.out.println("file not found :" + fileName);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                if (fws != null) fws.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }
}
