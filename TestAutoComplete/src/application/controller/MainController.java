package application.controller;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import application.model.Food;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;

public class MainController implements Initializable {
	@FXML
	private AnchorPane root;
	@FXML
	private TextField input;
	@FXML
	private Button foodButton;
	@FXML
	private ImageView imgFood; 
	@FXML
	private Label lblCalories;
	@FXML
	private Label lblCalFromFat;
	@FXML
	private Label lblTotalFat;
	@FXML 
	private Label tblTotalFatPercent;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		input.textProperty().addListener((observable, oldValue, newValue) -> {
			// Try it with an ArrayList
			ArrayList<String> possibleSelections = searchItemInstant(newValue);
			// Create a textfield binding and supply the object the field being bound and a list of possible selections
			AutoCompletionTextFieldBinding<String> autoCompletionTextFieldBinding = new AutoCompletionTextFieldBinding<>(input, SuggestionProvider.create(possibleSelections));
			// Only allow 8 rows of the results to be displayed
			autoCompletionTextFieldBinding.setVisibleRowCount(8);
		});	
	}

	/**
	 * handle will handle the events, which are occurring within the application scenes.
	 * @param event
	 */
	public void handle(ActionEvent event) {
		Food food = searchItemNutrients(input.getText());
		//lblCalories.setText(food.getCalories());
		//lblCalFromFat.setText(String.valueOf(Integer.parseInt(food.getFat())*9));
		//lblTotalFat.setText(food.getFat()+"g");
		//Image img = new Image(food.getImgURL());
		//imgFood.setImage(img);
	}	
	
	/**
	 * searchItemInstant accesses the API, which returns a JSON response, which is then parsed into an ArrayList of Strings that is 
	 * used to populate the autocomplete text box.
	 * @param s (String) 
	 * @return ArrayList: (String)
	 */
	public static ArrayList<String> searchItemInstant(String s){
		ArrayList<String> possibleSelections = new ArrayList<String>();
		String encodedUrl;
		try {
			encodedUrl = URLEncoder.encode(s, "UTF-8");
			HttpResponse<JsonNode> response = Unirest.get("https://trackapi.nutritionix.com/v2/search/instant?query="+encodedUrl)
					.header("x-app-id", "1c923ca6")
					.header("x-app-key", "8b7f96de69dccceb8e237d40c96b531d")
					.header("Accept", "application/json")
					.asJson(); 
			JSONArray jsonArray = response.getBody().getObject().getJSONArray("branded");
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				possibleSelections.add(jsonObj.get("food_name").toString());
				//m1.put(jsonObj.get("food_name").toString(), jsonObj.get("nix_item_id").toString());
			}			
		} catch (UnsupportedEncodingException | UnirestException | JSONException e) { 
			e.printStackTrace();
		} 
		//return m1;
		return possibleSelections; 
	}

	/**
	 * searchItemNutrients access the API to extract a single item, which is then parsed and displayed back to form elements within 
	 * the appropriate scene.
	 * @param s
	 * @return
	 */
	public static Food searchItemNutrients(String s){ 
		Food food = new Food("","",""); 
		try {
			System.out.println(s);
			HttpResponse<JsonNode> response = Unirest.post("https://trackapi.nutritionix.com/v2/natural/nutrients")
					.header("x-app-id", "1c923ca6")
					.header("x-app-key", "8b7f96de69dccceb8e237d40c96b531d")
					.header("content-type", "application/json")
					.body("{\"query\":\""+s+"\"}")
					.asJson(); 
			JSONArray jsonArray = response.getBody().getObject().getJSONArray("foods");
			for(int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i); 
				System.out.println(jsonObj.toString());
				//System.out.print(jsonObj.get("food_name").toString());
				//System.out.print(" - ");
				//System.out.println(jsonObj.get("nix_item_id").toString());
				food.setName(jsonObj.get("food_name").toString());
				food.setCalories(jsonObj.get("nf_calories").toString());
				food.setFat(jsonObj.get("nf_total_fat").toString());
				JSONObject photo = jsonObj.getJSONObject("photo");
				food.setImgURL(photo.getString("highres").toString());
				JSONArray fullNutrients = jsonObj.getJSONArray("full_nutrients");
				for(int j = 0; j < fullNutrients.length(); j++) {
					JSONObject jObj = fullNutrients.getJSONObject(j);
					System.out.println(jObj.toString());
				}
			}	
		} catch (UnirestException | JSONException e) {
			e.printStackTrace();
		} 
		return food;
		//return items; 
	}		
	
}
