package quotebook.theoneandonly.com.thequotebook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.util.ListIterator;
import java.util.Random;


public class QuoteBook extends ActionBarActivity {

    private GestureDetector gestureDetector;
    private  TextView quoteText;
    private RelativeLayout touch;

    private ListIterator quote;
    private String category=null;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            float deltaX = e2.getX() - e1.getX();


            if ((Math.abs(deltaX) < SWIPE_MIN_DISTANCE) || (Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY)) {
                return false; // insignificant swipe
            }
            else {
                if (deltaX < 0) { // left to right
                    next_quote();
                }
                else { // right to left
                    previous_quote();
                }
            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_book);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        touch = (RelativeLayout) findViewById(R.id.touch);
        quoteText = (TextView) findViewById(R.id.quote);
        quoteText.setMovementMethod(new ScrollingMovementMethod());
        gestureDetector=new GestureDetector(this,new SwipeGestureListener());

        touch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //gesture detector to detect swipe.
                gestureDetector.onTouchEvent(event);
                return true;//always return true to consume event
            }
        });

        quoteText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        //Set random color
        Random rnd=new Random();
        int color=Color.argb(255,200+rnd.nextInt(50),200+rnd.nextInt(50),200+rnd.nextInt(50));
        touch.setBackgroundColor(color);

        //Retrive Preferences

        SharedPreferences prefs=this.getSharedPreferences("com.QuoteBook.app", Context.MODE_PRIVATE);
        category=prefs.getString("com.QuoteBook.app.category",null);

        //Load quotes
        fetch_quotes();

    }
private void fetch_quotes(){

    //Set URL
    String url="http://www.goodreads.com/quotes/tag/" + category;
    if(category==null){
        url="http://www.goodreads.com/quotes";
    }

    //Fetch quotes
    try {
        //Connect
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        Elements quotes = doc.select("div.quoteText");
        quote = quotes.listIterator();

        //If no quote
        if(quote==null){
            Toast.makeText(this, "No Quotes for "+category, Toast.LENGTH_LONG).show();
            category=null;
            getCategory();//Try again
        }
        else{
            //Save Preferences
            SharedPreferences prefs=this.getSharedPreferences("com.QuoteBook.app", Context.MODE_PRIVATE);
            prefs.edit().putString("com.QuoteBook.app.category",category).apply();
            quoteText.setText("Category: "+category);
        }
    }
    catch (Exception e) {
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    }
}
    private void getCategory(){
        //Build dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Browse by Tag");
        final EditText input=new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Select",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category=input.getText().toString();
                //Fetch by category selected by user
                fetch_quotes();
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }
    public boolean OnTouchEvent(MotionEvent event){
        return gestureDetector.onTouchEvent(event);
    }

    private void previous_quote() {
        if(quote.hasPrevious()){
            String temp=Jsoup.parse(quote.previous().toString()).text();
            quoteText.setText(temp);
        }
        else
        {
            Toast.makeText(this,"First quote",Toast.LENGTH_SHORT).show();
        }
    }

    private void next_quote() {
        if(quote.hasNext()){
            String temp=Jsoup.parse(quote.next().toString()).text();
            quoteText.setText(temp);
        }
        else
        {
            Toast.makeText(this,"The End",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_quote_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.category) {
            //Display dialog box
            getCategory();
            return true;
        }
        else if(id==R.id.refresh){
            fetch_quotes();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}