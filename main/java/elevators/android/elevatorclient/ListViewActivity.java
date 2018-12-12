package elevators.android.elevatorclient;

import android.app.DialogFragment;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListViewActivity extends AppCompatActivity implements AskIpAddressDialog.AskIpAddressListener {

    private List<String> dataList = new ArrayList<>();                 // хранилище лога
    private BarGraphSeries<DataPoint> series = new BarGraphSeries<>(); // данные графика
    private HashMap<Integer, DataPoint> dataPoints = new HashMap<>();  // точки/столбики графика
    private ArrayAdapter<String> arrayAdapter;                         // адаптер для обновления listview
    private String client_ip = "127.0.0.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.arrayAdapterListView();
    }

    private void arrayAdapterListView()
    {
        setTitle("Elevator Net Client");

        // диалог для получения ip-адреса сервера
        AskIpAddressDialog ipDialog = new AskIpAddressDialog();
        ipDialog.show(getFragmentManager(), "getip");

        // вывод лога
        ListView listView = findViewById(R.id.list1);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(arrayAdapter);

        // вывод графика
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(series);

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });
        series.setSpacing(50);
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.RED);

    }

    // добавить в лог что-нибудь
    public void addDataList(String data) {
        dataList.add(data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                arrayAdapter.notifyDataSetChanged();
            }
        });
    }

    // добавить точку на график
    public void addDataPoint(int id, int count) {
        dataPoints.put(id, new DataPoint(id, count));
        series.resetData(generateData());
    }

    // собрать данные для графика
    private DataPoint[] generateData() {
        DataPoint[] values = new DataPoint[dataPoints.size()];
        int index = 0;
        for (Map.Entry<Integer, DataPoint> mapEntry : dataPoints.entrySet()) {
            values[index] = mapEntry.getValue();
            index++;
        }
        return values;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        AskIpAddressDialog d = (AskIpAddressDialog)dialog;
        client_ip = d.getInputdata();
        dataList.add("Server IP: " + client_ip);
        new Thread(new ClientThread(this, client_ip)).start();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // ничего не делать (хотя можно выйти)
    }
}

class ClientThread implements Runnable {

    private final String address;
    private final int port = 18943;

    private final ListViewActivity listView;
    private Socket socket;
    private BufferedReader in;

    public ClientThread(ListViewActivity listView, String serverip) {
        this.listView = listView;
        this.address = serverip;
    }

    @Override
    public void run() {
        String line = "";
        try {
            socket = new Socket(InetAddress.getByName(address), port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            while(!Thread.currentThread().isInterrupted()) {
                line = in.readLine();
                int id;
                int count;
                if(line.length()>0) {
                    switch(line.substring(0, 5)) {
                        // это статистика
                        case "#stt#":
                            switch(line.split(":")[1]) {
                                // лифт
                                case "elevt":
                                    id = Integer.valueOf(line.split(":")[2]) - 200;
                                    count = Integer.valueOf(line.split(":")[3]);
//                                    listView.addDataPoint(id, count);
                                    break;
                                // этаж
                                case "floor":
                                    id = Integer.valueOf(line.split(":")[2]);
                                    count = Integer.valueOf(line.split(":")[3]);
                                    listView.addDataPoint(id, count);
                                    break;
                            }
                            break;
                        // если не статистика - записать в лог
                        default:
                            listView.addDataList(line);
                            break;
                    }
                    Thread.sleep(500);
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
