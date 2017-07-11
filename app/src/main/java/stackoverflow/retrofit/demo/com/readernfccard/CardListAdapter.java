package stackoverflow.retrofit.demo.com.readernfccard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import stackoverflow.retrofit.demo.com.readernfccard.model.Card;

/**
 * Created by Admin on 7/11/2017.
 */

public class CardListAdapter extends ArrayAdapter<Card> {
    LayoutInflater mInflater;

    public CardListAdapter(Context context) {
        super(context, 0);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.card_view, parent, false);
        }

        Card card = getItem(position);

        TextView tv = (TextView) convertView.findViewById(R.id.date);
        tv.setText(card.getDate());

        tv = (TextView) convertView.findViewById(R.id.payment);
        tv.setText(card.getPayment() + "円 (Payment)");

        tv = (TextView) convertView.findViewById(R.id.device);
        tv.setText(card.getDevice());

        tv = (TextView) convertView.findViewById(R.id.in);
        tv.setText("入: " + card.getInStation() + " (" + card.getInLine() + ")");

        tv = (TextView) convertView.findViewById(R.id.out);
        tv.setText("出: " + card.getOutStation() + " (" + card.getOutLine() + ")");

        tv = (TextView) convertView.findViewById(R.id.balance);
        tv.setText("残高: " + card.getBalance() + "円");



        return convertView;
    }
}
