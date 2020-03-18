/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.desktop.components.paymentmethods;

import bisq.desktop.components.InputTextField;
import bisq.desktop.util.FormBuilder;
import bisq.desktop.util.Layout;
import bisq.desktop.util.validation.AustraliaPayIDValidator;

import bisq.core.account.witness.AccountAgeWitnessService;
import bisq.core.locale.Res;
import bisq.core.locale.TradeCurrency;
import bisq.core.payment.AustraliaPayID;
import bisq.core.payment.PaymentAccount;
import bisq.core.payment.payload.AustraliaPayIDPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.util.coin.CoinFormatter;
import bisq.core.util.validation.InputValidator;

import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import static bisq.desktop.util.FormBuilder.addCompactTopLabelTextField;
import static bisq.desktop.util.FormBuilder.addTopLabelTextField;

public class AustraliaPayIDForm extends PaymentMethodForm {
    private final AustraliaPayID australiaPayID;
    private final AustraliaPayIDValidator australiaPayIDValidator;
    private InputTextField mobileNrInputTextField;

    public static int addFormForBuyer(GridPane gridPane, int gridRow, PaymentAccountPayload paymentAccountPayload) {
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.account.owner"),
                ((AustraliaPayIDPayload) paymentAccountPayload).getBankAccountName());
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.payid"),
                ((AustraliaPayIDPayload) paymentAccountPayload).getPayid());
        return gridRow;
    }

    public AustraliaPayIDForm(PaymentAccount paymentAccount, AccountAgeWitnessService accountAgeWitnessService, AustraliaPayIDValidator australiaPayIDValidator,
                             InputValidator inputValidator, GridPane gridPane, int gridRow, CoinFormatter formatter) {
        super(paymentAccount, accountAgeWitnessService, inputValidator, gridPane, gridRow, formatter);
        this.australiaPayID = (AustraliaPayID) paymentAccount;
        this.australiaPayIDValidator = australiaPayIDValidator;
    }

    @Override
    public void addFormForAddAccount() {
        gridRowFrom = gridRow + 1;

        InputTextField holderNameInputTextField = FormBuilder.addInputTextField(gridPane, ++gridRow,
                Res.get("payment.account.owner"));
        holderNameInputTextField.setValidator(inputValidator);
        holderNameInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            australiaPayID.setBankAccountName(newValue);
            updateFromInputs();
        });

        mobileNrInputTextField = FormBuilder.addInputTextField(gridPane, ++gridRow, Res.get("payment.payid"));
        mobileNrInputTextField.setValidator(australiaPayIDValidator);
        mobileNrInputTextField.textProperty().addListener((ov, oldValue, newValue) -> {
            australiaPayID.setPayID(newValue);
            updateFromInputs();
        });

        TradeCurrency singleTradeCurrency = australiaPayID.getSingleTradeCurrency();
        String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "null";
        addTopLabelTextField(gridPane, ++gridRow, Res.get("shared.currency"), nameAndCode);
        addLimitations(false);
        addAccountNameTextFieldWithAutoFillToggleButton();
    }

    @Override
    protected void autoFillNameTextField() {
        setAccountNameWithString(mobileNrInputTextField.getText());
    }

    @Override
    public void addFormForDisplayAccount() {
        gridRowFrom = gridRow;
        addTopLabelTextField(gridPane, gridRow, Res.get("payment.account.name"),
                australiaPayID.getAccountName(), Layout.FIRST_ROW_AND_GROUP_DISTANCE);
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("shared.paymentMethod"),
                Res.get(australiaPayID.getPaymentMethod().getId()));
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.payid"),
                australiaPayID.getPayID());
        TextField field = addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("payment.account.owner"),
                australiaPayID.getBankAccountName()).second;
        field.setMouseTransparent(false);
        TradeCurrency singleTradeCurrency = australiaPayID.getSingleTradeCurrency();
        String nameAndCode = singleTradeCurrency != null ? singleTradeCurrency.getNameAndCode() : "null";
        addCompactTopLabelTextField(gridPane, ++gridRow, Res.get("shared.currency"), nameAndCode);
        addLimitations(true);
    }

    @Override
    public void updateAllInputsValid() {
        allInputsValid.set(isAccountNameValid()
                && australiaPayIDValidator.validate(australiaPayID.getPayID()).isValid
                && inputValidator.validate(australiaPayID.getBankAccountName()).isValid
                && australiaPayID.getTradeCurrencies().size() > 0);
    }
}
