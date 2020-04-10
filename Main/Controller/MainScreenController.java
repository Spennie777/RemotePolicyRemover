package Main.Controller;

import Main.Main;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.text.ParseException;

public class MainScreenController {

    @FXML public TextField tfUsername, tfCutOffDate, tfServerURL;
    @FXML public PasswordField pwfPassword;
    @FXML public Text txtError, txtPolicies, txtDateError, txtOldestDate;

    private Main main;

    public void setMain(Main main){
        this.main = main;
    }

    public void submit() {
        String username = tfUsername.getText();
        String password = pwfPassword.getText();
        String serverURL = tfServerURL.getText();
        if (username.isEmpty() || password.isEmpty() || serverURL.isEmpty()) {
            txtError.setText("Please fill in server URL, username, and password");
            txtPolicies.setText("");
            txtOldestDate.setText("");
            return;
        }
        main.getPolicies(serverURL, username, password);
    }

    public void setTxtError(String error) {
        txtError.setText(error);
    }

    public String getTxtPolicies() {
        return txtPolicies.getText();
    }

    public void setTxtPolicies(String policies) {
        txtPolicies.setText(policies);
    }

    public void setTxtOldestDate(String date) {
        txtOldestDate.setText(date);
    }

    public void deleteOlderThan() throws ParseException {
        if (tfCutOffDate.getText().matches("\\d{4}-\\d{2}-\\d{2}")) {
            txtDateError.setText("");
            String username = tfUsername.getText();
            String password = pwfPassword.getText();
            if (username.isEmpty() || password.isEmpty()) {
                txtError.setText("Please fill in username and password");
                return;
            }
            txtError.setText("");
            String cutOffDate = tfCutOffDate.getText();
            main.removePolicies(cutOffDate, username, password);
            return;
        }
        txtDateError.setText("Please enter date as YYYY-MM-DD");
    }
}
