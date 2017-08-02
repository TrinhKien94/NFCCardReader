package stackoverflow.retrofit.demo.com.readernfccard;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import stackoverflow.retrofit.demo.com.readernfccard.lib.FeliCa;
import stackoverflow.retrofit.demo.com.readernfccard.model.Card;

public class MainActivity extends AppCompatActivity {

    private CardListAdapter adapter;
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_listview);

        adapter = new CardListAdapter(getApplicationContext());

        int padding = (int) (getResources().getDisplayMetrics().density * 8);
        ListView listView = (ListView) findViewById(R.id.card_list);
        listView.setPadding(padding, 0, padding, 0);
        listView.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        listView.setDivider(null);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View header = inflater.inflate(R.layout.list_header_footer, listView, false);
        View footer = inflater.inflate(R.layout.list_header_footer, listView, false);
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);
        listView.setAdapter(adapter);

        // NFC(FeliCa) ID を取得
        byte[] felicaIDm;
        Intent intent = getIntent();
        Tag nfcTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        Log.i("")
        if(nfcTag != null) {
            felicaIDm = nfcTag.getId();
            String [] test = nfcTag.getTechList();
            Log.i("NFCTYPE",test[0]);
//            for(String t : test){
//                Log.i("Test",t);
//            }
            Log.i("test",bytesToHex(nfcTag.getId()));
            Log.i("tagID",nfcTag.getId().hashCode()+"");
            Log.i("tagID",nfcTag.getId().toString());
            Log.i("tag", String.valueOf(nfcTag.describeContents()));
            Toast.makeText(this, "ID Card " + felicaIDm, Toast.LENGTH_LONG).show();

        }else {
            Toast.makeText(this, "カードを後ろにかざしてください", Toast.LENGTH_LONG).show();
            return;
        }

        NfcF felica = NfcF.get(nfcTag);

        try {
            felica.connect();
            byte[] req = FeliCa.readWithoutEncryption(felicaIDm, 10);
            byte[] res = felica.transceive(req);
            felica.close();
            parsePasmoHistory(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parsePasmoHistory(byte[] res) throws Exception {
        // res[0] = データ長
        // res[1] = 0x07
        // res[2〜9] = カードID
        // res[10,11] = エラーコード。0=正常。
        if (res[10] != 0x00) {
            throw new RuntimeException("Felica error.");
        }

        // res[12] = 応答ブロック数
        // res[13+n*16] = 履歴データ。16byte/ブロックの繰り返し。
        int size = res[12];
        int payment = 0;
        for (int i = 0; i < size; i++) {
            FeliCa felica = FeliCa.parse(res, 13 + i * 16);
            Card card = Card.getCard(getBaseContext(), felica);
            if (i < size-1) {
                FeliCa nextFelica = FeliCa.parse(res, 13 + (i+1) * 16);
                Card nextCard = Card.getCard(getBaseContext(), nextFelica);
                payment = Integer.parseInt(card.getBalance()) - Integer.parseInt(nextCard.getBalance());
            }
            card.setPayment(String.valueOf(payment));
            adapter.add(card);
        }
    }
}
