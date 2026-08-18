package org.web3j.tx.gas;

import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthMaxPriorityFeePerGas;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CappedDynamicEIP1559GasProviderTest {

	@Test
	void capsMaxFeePerGas() throws Exception {
		Web3j web3j = mock(Web3j.class);
		Request<?, EthBlock> blockRequest = mock(Request.class);
		EthBlock blockResponse = mock(EthBlock.class);
		EthBlock.Block block = mock(EthBlock.Block.class);
		Request<?, EthMaxPriorityFeePerGas> priorityFeeRequest = mock(Request.class);
		EthMaxPriorityFeePerGas priorityFeeResponse = mock(EthMaxPriorityFeePerGas.class);

		doReturn(blockRequest).when(web3j).ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false);
		when(blockRequest.send()).thenReturn(blockResponse);
		when(blockResponse.getBlock()).thenReturn(block);
		when(block.getBaseFeePerGas()).thenReturn(BigInteger.valueOf(100));
		doReturn(priorityFeeRequest).when(web3j).ethMaxPriorityFeePerGas();
		when(priorityFeeRequest.send()).thenReturn(priorityFeeResponse);
		when(priorityFeeResponse.getMaxPriorityFeePerGas()).thenReturn(BigInteger.valueOf(20));

		CappedDynamicEIP1559GasProvider gasProvider = new CappedDynamicEIP1559GasProvider(
				web3j,
				1L,
				PriorityGasProvider.Priority.NORMAL,
				BigDecimal.ONE,
				BigInteger.valueOf(150));

		assertEquals(BigInteger.valueOf(150), gasProvider.getMaxFeePerGas());
	}

	@Test
	void rejectsNonPositiveMaxFeePerGas() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new CappedDynamicEIP1559GasProvider(
						mock(Web3j.class),
						1L,
						PriorityGasProvider.Priority.NORMAL,
						BigDecimal.ONE,
						BigInteger.ZERO));
	}
}
