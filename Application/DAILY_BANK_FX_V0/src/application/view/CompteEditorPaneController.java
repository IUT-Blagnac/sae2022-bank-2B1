package application.view;

/**
 * Fenetre de gestion des comptes (ajout, modification)
 */

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import application.DailyBankState;
import application.tools.AlertUtilities;
import application.tools.ConstantesIHM;
import application.tools.EditionMode;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.data.Client;
import model.data.CompteCourant;

public class CompteEditorPaneController implements Initializable {

	// Etat application
	private DailyBankState dbs;

	// Fenêtre physique
	private Stage primaryStage;

	// Données de la fenêtre
	private EditionMode em;
	private Client clientDuCompte;
	private CompteCourant compteEdite;
	private CompteCourant compteResult;

	// Manipulation de la fenêtre
	/**
	 * Définit les variables de la fenetre
	 * 
	 * @param _primaryStage : scene
	 * @param _dbstate : données de la session de l'utilisateur
	 */
	public void initContext(Stage _primaryStage, DailyBankState _dbstate) {
		this.primaryStage = _primaryStage;
		this.dbs = _dbstate;
		this.configure();
	}

	private void configure() {
		this.primaryStage.setOnCloseRequest(e -> this.closeWindow(e));

		this.txtDecAutorise.focusedProperty().addListener((t, o, n) -> this.focusDecouvert(t, o, n));
		this.txtSolde.focusedProperty().addListener((t, o, n) -> this.focusSolde(t, o, n));
	}

	/**
	 * Affiche la fenetre et attend une action, la fenetre change en fonction du mode (ajout, modification)
	 * @param client : le client auquel appartient le compte
	 * @param cpte : le compte à modifier (null si ajout)
	 * @param mode : le mode d'edition (ajout, modification)
	 * @return : le client modifié ou non
	 */
	public CompteCourant displayDialog(Client client, CompteCourant cpte, EditionMode mode) {		
		this.clientDuCompte = client;
		this.em = mode;
		if (cpte == null) {
			this.compteEdite = new CompteCourant(0, 200, 0, "N", this.clientDuCompte.idNumCli);

		} else {
			this.compteEdite = new CompteCourant(cpte);
		}
		this.compteResult = null;
		this.txtIdclient.setDisable(true);
		this.txtIdAgence.setDisable(true);
		this.txtIdNumCompte.setDisable(true);
		switch (mode) {
			case CREATION:
				this.txtDecAutorise.setDisable(false);
				this.txtSolde.setDisable(false);
				this.lblMessage.setText("Informations sur le nouveau compte");
				this.lblSolde.setText("Solde (premier dépôt)");
				this.btnOk.setText("Ajouter");
				this.btnCancel.setText("Annuler");
				break;
			case MODIFICATION:
				AlertUtilities.showAlert(this.primaryStage, "Non implémenté", "Modif de compte n'est pas implémenté", null,
						AlertType.ERROR);
				return null;
			// break;
			case SUPPRESSION:
				this.txtDecAutorise.setDisable(true);
				this.txtSolde.setDisable(true);
				this.lblSolde.setText("Solde");
				this.lblMessage.setText("Suppresion du compte");
				this.btnOk.setText("Supprimer");
				this.btnCancel.setText("Annuler");
				break;
		}
		
		// Paramétrages spécifiques pour les chefs d'agences
		if (ConstantesIHM.isAdmin(this.dbs.getEmpAct())) {
			// rien pour l'instant
		}

		// initialisation du contenu des champs
		this.txtIdclient.setText("" + this.compteEdite.idNumCli);
		this.txtIdNumCompte.setText("" + this.compteEdite.idNumCompte);
		this.txtIdAgence.setText("" + this.dbs.getEmpAct().idAg);
		this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde).trim());

		this.compteResult = null;

		this.primaryStage.showAndWait();
		return this.compteResult;
	}

	// Gestion du stage
	private Object closeWindow(WindowEvent e) {
		this.doCancel();
		e.consume();
		return null;
	}

	private Object focusDecouvert(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				int val;
				val = Integer.parseInt(this.txtDecAutorise.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.debitAutorise = val;
			} catch (NumberFormatException nfe) {
				this.txtDecAutorise.setText("" + this.compteEdite.debitAutorise);
			}
		}
		return null;
	}

	private Object focusSolde(ObservableValue<? extends Boolean> txtField, boolean oldPropertyValue,
			boolean newPropertyValue) {
		if (oldPropertyValue) {
			try {
				double val;
				val = Double.parseDouble(this.txtSolde.getText().trim());
				if (val < 0) {
					throw new NumberFormatException();
				}
				this.compteEdite.solde = val;
			} catch (NumberFormatException nfe) {
				this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde).trim());
			}
		}
		this.txtSolde.setText(String.format(Locale.ENGLISH, "%10.02f", this.compteEdite.solde).trim());
		return null;
	}

	// Attributs de la scene + actions
	@FXML
	private Label lblMessage;
	@FXML
	private Label lblSolde;
	@FXML
	private TextField txtIdclient;
	@FXML
	private TextField txtIdAgence;
	@FXML
	private TextField txtIdNumCompte;
	@FXML
	private TextField txtDecAutorise;
	@FXML
	private TextField txtSolde;
	@FXML
	private Button btnOk;
	@FXML
	private Button btnCancel;

	/**
	 * Redéfinition de la fonction initialize
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	@FXML
	private void doCancel() {
		this.compteResult = null;
		this.primaryStage.close();
	}

	@FXML
	private void doAjouter() {
		switch (this.em) {
		case CREATION:
			if (this.isSaisieValide()) {
				this.compteResult = this.compteEdite;
				this.primaryStage.close();
			} else
				AlertUtilities.showAlert(primaryStage, "Erreur de saisie", "Erreur de saisie", "Le solde du compte doit être compris entre 50€ et 999999€ minimum et le découvert autorisé entre 0€ et 9999€", AlertType.ERROR);
			break;
		case MODIFICATION:
			if (this.isSaisieValide()) {
				this.compteResult = this.compteEdite;
				this.primaryStage.close();
			}
			break;
		case SUPPRESSION:
			this.compteResult = this.compteEdite;
			this.primaryStage.close();
			break;
		}

	}

	private boolean isSaisieValide() {
		return this.compteEdite.solde >= 50 && this.compteEdite.debitAutorise >= 0 && this.compteEdite.solde < 1000000 && this.compteEdite.debitAutorise < 10000;
	}
}
