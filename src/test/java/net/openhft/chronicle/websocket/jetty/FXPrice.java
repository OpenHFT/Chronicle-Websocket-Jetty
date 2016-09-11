/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.chronicle.wire.WireType;

class FXPrice implements Marshallable {
    public double bidprice;
    public double offerprice;
    //enum
    public CcyPair pair;
    public int size;
    public byte level;
    public String exchangeName;
    public transient double midPrice;

    @Override
    public void readMarshallable(WireIn wire) throws IORuntimeException {
        wire.read(() -> "bidprice").float64(this, (t, v) -> t.bidprice = v)
                .read(() -> "offerprice").float64(this, (t, v) -> t.offerprice = v)
                .read(() -> "pair").asEnum(CcyPair.class, this, (t, v) -> t.pair = v)
                .read(() -> "size").int32(this, (t, v) -> t.size = v)
                .read(() -> "level").int8(this, (t, v) -> t.level = v)
                .read(() -> "exchangeName").text(this, (t, v) -> t.exchangeName = v);
        midPrice = (bidprice + offerprice) / 2;
    }

    @Override
    public void writeMarshallable(WireOut wire) {
        wire.write(() -> "bidprice").float64(bidprice)
                .write(() -> "offerprice").float64(offerprice)
                .write(() -> "pair").asEnum(pair)
                .write(() -> "size").int32(size)
                .write(() -> "level").int8(level)
                .write(() -> "exchangeName").text(exchangeName);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FXPrice && toString().equals(obj.toString());
    }

    @Override
    public String toString() {
        return WireType.TEXT.asString(this);
    }
}