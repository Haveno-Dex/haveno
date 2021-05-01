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

package bisq.core.trade.messages;

import bisq.common.crypto.PubKeyRing;
import bisq.common.proto.ProtoUtil;
import bisq.core.proto.CoreProtoResolver;
import bisq.network.p2p.DirectMessage;
import bisq.network.p2p.NodeAddress;
import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Value
public final class InitMultisigMessage extends TradeMessage implements DirectMessage {
    private final NodeAddress senderNodeAddress;
    private final PubKeyRing pubKeyRing;
    private final long currentDate;
    @Nullable
    private final String preparedMultisigHex;
    @Nullable
    private final String madeMultisigHex;

    public InitMultisigMessage(String tradeId,
                               NodeAddress senderNodeAddress,
                               PubKeyRing pubKeyRing,
                               String uid,
                               int messageVersion,
                               long currentDate,
                               String preparedMultisigHex,
                               String madeMultisigHex) {
        super(messageVersion, tradeId, uid);
        this.senderNodeAddress = senderNodeAddress;
        this.pubKeyRing = pubKeyRing;
        this.currentDate = currentDate;
        this.preparedMultisigHex = preparedMultisigHex;
        this.madeMultisigHex = madeMultisigHex;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public protobuf.NetworkEnvelope toProtoNetworkEnvelope() {
        protobuf.InitMultisigMessage.Builder builder = protobuf.InitMultisigMessage.newBuilder()
                .setTradeId(tradeId)
                .setSenderNodeAddress(senderNodeAddress.toProtoMessage())
                .setPubKeyRing(pubKeyRing.toProtoMessage())
                .setUid(uid);

        Optional.ofNullable(preparedMultisigHex).ifPresent(e -> builder.setPreparedMultisigHex(preparedMultisigHex));
        Optional.ofNullable(madeMultisigHex).ifPresent(e -> builder.setMadeMultisigHex(madeMultisigHex));

        builder.setCurrentDate(currentDate);

        return getNetworkEnvelopeBuilder().setInitMultisigMessage(builder).build();
    }

    public static InitMultisigMessage fromProto(protobuf.InitMultisigMessage proto,
                                                CoreProtoResolver coreProtoResolver,
                                                int messageVersion) {
        return new InitMultisigMessage(proto.getTradeId(),
                NodeAddress.fromProto(proto.getSenderNodeAddress()),
                PubKeyRing.fromProto(proto.getPubKeyRing()),
                proto.getUid(),
                messageVersion,
                proto.getCurrentDate(),
                ProtoUtil.stringOrNullFromProto(proto.getPreparedMultisigHex()),
                ProtoUtil.stringOrNullFromProto(proto.getMadeMultisigHex()));
    }

    @Override
    public String toString() {
        return "MultisigMessage {" +
                "\n     senderNodeAddress=" + senderNodeAddress +
                ",\n     pubKeyRing=" + pubKeyRing +
                ",\n     currentDate=" + currentDate +
                ",\n     preparedMultisigHex='" + preparedMultisigHex +
                ",\n     madeMultisigHex='" + madeMultisigHex +
                "\n} " + super.toString();
    }
}
