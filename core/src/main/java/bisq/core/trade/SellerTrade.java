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

package bisq.core.trade;

import static com.google.common.base.Preconditions.checkNotNull;

import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.locale.CurrencyUtil;
import bisq.core.offer.Offer;
import bisq.core.trade.protocol.ProcessModel;
import bisq.network.p2p.NodeAddress;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;

@Slf4j
public abstract class SellerTrade extends Trade {
    SellerTrade(Offer offer,
                Coin tradeAmount,
                Coin txFee,
                Coin takerFee,
                long tradePrice,
                @Nullable NodeAddress makerNodeAddress,
                @Nullable NodeAddress takerNodeAddress,
                @Nullable NodeAddress arbitratorNodeAddress,
                XmrWalletService xmrWalletService,
                ProcessModel processModel) {
        super(offer,
                tradeAmount,
                txFee,
                takerFee,
                tradePrice,
                makerNodeAddress,
                takerNodeAddress,
                arbitratorNodeAddress,
                xmrWalletService,
                processModel);
    }

    SellerTrade(Offer offer,
                Coin txFee,
                Coin takeOfferFee,
                @Nullable NodeAddress makerNodeAddress,
                @Nullable NodeAddress takerNodeAddress,
                @Nullable NodeAddress arbitratorNodeAddress,
                XmrWalletService xmrWalletService,
                ProcessModel processModel) {
        super(offer,
                txFee,
                takeOfferFee,
                makerNodeAddress,
                takerNodeAddress,
                arbitratorNodeAddress,
                xmrWalletService,
                processModel);
    }

    @Override
    public Coin getPayoutAmount() {
        return checkNotNull(getOffer()).getSellerSecurityDeposit();
    }

    @Override
    public boolean confirmPermitted() {
        // For altcoin there is no reason to delay BTC release as no chargeback risk
        if (CurrencyUtil.isCryptoCurrency(getOffer().getCurrencyCode())) {
            return true;
        }

        switch (getDisputeState()) {
            case NO_DISPUTE:
                return true;

            case DISPUTE_REQUESTED:
            case DISPUTE_STARTED_BY_PEER:
            case DISPUTE_CLOSED:
            case MEDIATION_REQUESTED:
            case MEDIATION_STARTED_BY_PEER:
                return false;

            case MEDIATION_CLOSED:
                return !mediationResultAppliedPenaltyToSeller();

            case REFUND_REQUESTED:
            case REFUND_REQUEST_STARTED_BY_PEER:
            case REFUND_REQUEST_CLOSED:
            default:
                return false;
        }
    }
}

