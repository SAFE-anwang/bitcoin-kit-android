package net.bither.bitherj.core;

import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.hd.HDKeyDerivation;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicWordList;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class HDAccountColdTest {

    @Test
    public void testWords() throws MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException, MnemonicException.MnemonicLengthException, IOException {
        SecureCharSequence password = new SecureCharSequence("zm19880219");

        String words = "impose among margin weapon hope allow spin main victory result miss genuine";
        List<String> wordList = Arrays.stream(words.split(" ")).collect(Collectors.toList());

        MnemonicCodeTestClass mnemonicCode = new MnemonicCodeTestClass();

        System.out.println("wordList = " + wordList);
        byte[] mnemonicCodeSeed =mnemonicCode.toEntropy(wordList);
        HDAccount restoreHdAccount = new HDAccount(mnemonicCode, mnemonicCodeSeed, password);

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(restoreHdAccount.hdSeed);
        DeterministicKey accountKey = getAccount(master);
        DeterministicKey rootKey = getChainRootKey(accountKey, 0);
        DeterministicKey addressKey = rootKey.deriveSoftened(0);

        System.out.println("safegem address = " + addressKey.toAddress());

        password.wipe();

    }

    protected DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    protected DeterministicKey getChainRootKey(DeterministicKey accountKey, int value) {
        return accountKey.deriveSoftened(value);
    }

    public static class MnemonicCodeTestClass extends MnemonicCode {
        private static final String WordListPath = "wordlist/english.txt";

        public MnemonicCodeTestClass() throws IOException {
            super();
        }

        @Override
        protected HashMap<MnemonicWordList, InputStream> openWordList() throws IOException, IllegalArgumentException {
            return getAllMnemonicWordListRawResources();
        }

        private HashMap<MnemonicWordList, InputStream> getAllMnemonicWordListRawResources() {
            ArrayList<MnemonicWordList> mnemonicWordLists = MnemonicWordList.getAllMnemonicWordLists();
            HashMap<MnemonicWordList, InputStream> inputStreamMap = new HashMap<>();
            for (MnemonicWordList mnemonicWordList: mnemonicWordLists) {
                inputStreamMap.put(mnemonicWordList, getMnemonicWordListRawResource(mnemonicWordList));
            }
            return inputStreamMap;
        }

        private InputStream getMnemonicWordListRawResource(MnemonicWordList wordList) {
            return MnemonicCode.class.getResourceAsStream(WordListPath);
        }
    }

//    KEY_PATH_BTC(0),
//    KEY_PATH_SAFE(1),
//    KEY_PATH_LTC(2),
//    KEY_PATH_BCH(3),
//    KEY_PATH_BTG(4),
//    KEY_PATH_DASH(5),
//    KEY_PATH_QTUM(6),
//    KEY_PATH_ETH(7),
//    KEY_PATH_FTO(8),
//    KEY_PATH_ETC(9),
//    KEY_PATH_BSV(10);

}
