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

package bisq.core.trade.protocol.tasks.taker;

import bisq.common.taskrunner.TaskRunner;
import bisq.core.btc.model.XmrAddressEntry;
import bisq.core.btc.wallet.TradeWalletService;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.dao.exceptions.DaoDisabledException;
import bisq.core.trade.Trade;
import bisq.core.trade.protocol.tasks.TradeTask;
import lombok.extern.slf4j.Slf4j;
import monero.wallet.model.MoneroTxWallet;

@Slf4j
public class TakerCreateReserveTradeTx extends TradeTask {
  @SuppressWarnings({ "unused" })
  public TakerCreateReserveTradeTx(TaskRunner taskHandler, Trade trade) {
    super(taskHandler, trade);
  }

  @Override
  protected void run() {
    try {
      runInterceptHook();

      XmrWalletService walletService = processModel.getXmrWalletService();
      String id = processModel.getOffer().getId();
      XmrAddressEntry reservedForTradeAddressEntry = walletService.getOrCreateAddressEntry(id, XmrAddressEntry.Context.RESERVED_FOR_TRADE);
      TradeWalletService tradeWalletService = processModel.getTradeWalletService();
      String feeReceiver = "52FnB7ABUrKJzVQRpbMNrqDFWbcKLjFUq8Rgek7jZEuB6WE2ZggXaTf4FK6H8gQymvSrruHHrEuKhMN3qTMiBYzREKsmRKM"; // TODO (woodser): don't hardcode
      
      // pay trade fee to reserve trade
      MoneroTxWallet tx = tradeWalletService.createXmrTradingFeeTx(
              reservedForTradeAddressEntry.getAddressString(),
              processModel.getFundsNeededForTradeAsLong(),
              trade.getTakerFee(),
              trade.getTxFee(),
              feeReceiver,
              false);

      trade.setTakerFeeTxId(tx.getHash());
      processModel.setTakeOfferFeeTx(tx);
      trade.persist();
      complete();
    } catch (Throwable t) {
      if (t instanceof DaoDisabledException) {
        failed("You cannot pay the trade fee in BSQ at the moment because the DAO features have been " + "disabled due technical problems. Please use the BTC fee option until the issues are resolved. " + "For more information please visit the Bisq Forum.");
      } else {
        failed(t);
      }
    }
  }
}
