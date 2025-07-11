package com.anwang.safewallet.safekit

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import io.horizontalsystems.bitcoincore.AbstractKit
import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.BitcoinCore.SyncMode
import io.horizontalsystems.bitcoincore.BitcoinCoreBuilder
import io.horizontalsystems.bitcoincore.DustCalculator
import io.horizontalsystems.bitcoincore.apisync.BiApiTransactionProvider
import io.horizontalsystems.bitcoincore.apisync.InsightApi
import io.horizontalsystems.bitcoincore.apisync.blockchair.BlockchairApi
import io.horizontalsystems.bitcoincore.apisync.blockchair.BlockchairBlockHashFetcher
import io.horizontalsystems.bitcoincore.apisync.blockchair.BlockchairTransactionProvider
import io.horizontalsystems.bitcoincore.blocks.BlockMedianTimeHelper
import io.horizontalsystems.bitcoincore.blocks.validators.BlockValidatorChain
import io.horizontalsystems.bitcoincore.blocks.validators.BlockValidatorSet
import io.horizontalsystems.bitcoincore.extensions.hexToByteArray
import io.horizontalsystems.bitcoincore.managers.*
import io.horizontalsystems.bitcoincore.models.Address
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoincore.models.Checkpoint
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.network.Network
import io.horizontalsystems.bitcoincore.storage.CoreDatabase
import io.horizontalsystems.bitcoincore.storage.Storage
import io.horizontalsystems.bitcoincore.transactions.TransactionSizeCalculator
import io.horizontalsystems.bitcoincore.utils.Base58AddressConverter
import io.horizontalsystems.bitcoincore.utils.IAddressConverter
import io.horizontalsystems.bitcoincore.utils.MerkleBranch
import io.horizontalsystems.bitcoincore.utils.PaymentAddressParser
import io.horizontalsystems.bitcoincore.utils.SegwitAddressConverter
import io.horizontalsystems.dashkit.DashKit
import io.horizontalsystems.dashkit.IInstantTransactionDelegate
import io.horizontalsystems.dashkit.InstantSend
import io.horizontalsystems.dashkit.MainNetDash
import io.horizontalsystems.dashkit.TestNetDash
import io.horizontalsystems.dashkit.X11Hasher
import io.horizontalsystems.dashkit.core.DashTransactionInfoConverter
import io.horizontalsystems.dashkit.core.SingleSha256Hasher
import io.horizontalsystems.dashkit.instantsend.*
import io.horizontalsystems.dashkit.instantsend.instantsendlock.InstantSendLockHandler
import io.horizontalsystems.dashkit.instantsend.instantsendlock.InstantSendLockManager
import io.horizontalsystems.dashkit.instantsend.transactionlockvote.TransactionLockVoteHandler
import io.horizontalsystems.dashkit.instantsend.transactionlockvote.TransactionLockVoteManager
import io.horizontalsystems.dashkit.managers.*
import io.horizontalsystems.dashkit.masternodelist.*
import io.horizontalsystems.dashkit.messages.*
import io.horizontalsystems.dashkit.models.CoinbaseTransactionSerializer
import io.horizontalsystems.dashkit.models.DashTransactionInfo
import io.horizontalsystems.dashkit.models.InstantTransactionState
import io.horizontalsystems.dashkit.storage.DashKitDatabase
import io.horizontalsystems.dashkit.storage.DashStorage
import io.horizontalsystems.dashkit.tasks.PeerTaskFactory
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.horizontalsystems.hodler.HodlerPlugin

class SafeKit : AbstractKit, IInstantTransactionDelegate, BitcoinCore.Listener {
    enum class NetworkType {
        MainNet,
        TestNet
    }

    interface Listener {
        fun onTransactionsUpdate(inserted: List<DashTransactionInfo>, updated: List<DashTransactionInfo>)
        fun onTransactionsDelete(hashes: List<String>)
        fun onBalanceUpdate(balance: BalanceInfo)
        fun onLastBlockInfoUpdate(blockInfo: BlockInfo)
        fun onKitStateUpdate(state: BitcoinCore.KitState)
    }

    var listener: Listener? = null
    var mainNetSafe: MainNetSafe? = null

    var fallbackBlockDate: String? = null
        set(value) {
            field = value
            mainNetSafe?.fallbackBlockDate = value
        }

    public override var bitcoinCore: BitcoinCore
    override var network: Network

    private val dashStorage: DashStorage
    private val instantSend: InstantSend
    private val dashTransactionInfoConverter: DashTransactionInfoConverter
    private val confirmedUnlockedUnspentOutputProvider: ConfirmedUnlockedUnspentOutputProvider

    fun getConfirmedUnlockedUnspentOutputProvider(): ConfirmedUnlockedUnspentOutputProvider{
        return confirmedUnlockedUnspentOutputProvider
    }

    constructor(
            context: Context,
            words: List<String>,
            passphrase: String,
            walletId: String,
            networkType: NetworkType = defaultNetworkType,
            peerSize: Int = defaultPeerSize,
            syncMode: SyncMode = defaultSyncMode,
            confirmationsThreshold: Int = defaultConfirmationsThreshold,
            isAnBaoWallet: Boolean = false,
            isSafe3Wallet: Boolean = false
    ) : this(context, Mnemonic().toSeed(words, passphrase), walletId, networkType, peerSize, syncMode, confirmationsThreshold, isAnBaoWallet, isSafe3Wallet)

    constructor(
        context: Context,
        seed: ByteArray,
        walletId: String,
        networkType: NetworkType = defaultNetworkType,
        peerSize: Int = defaultPeerSize,
        syncMode: SyncMode = defaultSyncMode,
        confirmationsThreshold: Int = defaultConfirmationsThreshold,
        isAnBaoWallet: Boolean = false,
        isSafe3Wallet: Boolean = false
    ) : this(context, HDExtendedKey(seed, HDWallet.Purpose.BIP44), walletId, networkType, peerSize, syncMode, confirmationsThreshold, isAnBaoWallet, isSafe3Wallet)

    constructor(
            context: Context,
            extendedKey: HDExtendedKey,
            walletId: String,
            networkType: NetworkType = defaultNetworkType,
            peerSize: Int = defaultPeerSize,
            syncMode: SyncMode = defaultSyncMode,
            confirmationsThreshold: Int = defaultConfirmationsThreshold,
            isAnBaoWallet: Boolean = false,
            isSafe3Wallet: Boolean = false
    ) {
        val coreDatabase = CoreDatabase.getInstance(context, getDatabaseNameCore(networkType, walletId, syncMode))
        val dashDatabase = DashKitDatabase.getInstance(context, getDatabaseName(networkType, walletId, syncMode))
        val initialSyncUrl: String

        val coreStorage = Storage(coreDatabase)
        dashStorage = DashStorage(dashDatabase, coreStorage)

        network = when (networkType) {
            NetworkType.MainNet -> {
//                initialSyncUrl = "https://dash.horizontalsystems.xyz/apg"
                initialSyncUrl = "https://chain.anwang.org/insight-api-safe"
                mainNetSafe = MainNetSafe(context)
                mainNetSafe!!
            }
            NetworkType.TestNet -> {
                initialSyncUrl = "http://dash-testnet.horizontalsystems.xyz/apg"
                TestNetSafe()
            }
        }

        val checkpoint = Checkpoint.resolveCheckpoint(syncMode, network, coreStorage)
        val paymentAddressParser = PaymentAddressParser("dash", removeScheme = true)
        val instantTransactionManager = InstantTransactionManager(dashStorage, InstantSendFactory(), InstantTransactionState())

        val apiSyncStateManager = ApiSyncStateManager(coreStorage, network.syncableFromApi && syncMode !is SyncMode.Full)

        val apiTransactionProvider = apiTransactionProvider(networkType, syncMode, apiSyncStateManager)

        dashTransactionInfoConverter = DashTransactionInfoConverter(instantTransactionManager)

//        val blockHelper = BlockValidatorHelper(coreStorage)

        val blockValidatorSet = BlockValidatorSet()
        blockValidatorSet.addBlockValidator(SafeSPOSBlockValidator())

        val blockValidatorChain = BlockValidatorChain()
//        if (network is MainNetSafe) {
//            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 68589))
//        } else {
//            blockValidatorChain.add(DarkGravityWaveTestnetValidator(targetSpacing, targetTimespan, maxTargetBits, 4002))
//            blockValidatorChain.add(DarkGravityWaveValidator(blockHelper, heightInterval, targetTimespan, maxTargetBits, 4002))
//        }
        blockValidatorSet.addBlockValidator(blockValidatorChain)

        val coreBuilder = BitcoinCoreBuilder()
        val hodlerPlugin = hodlerPlugin(coreStorage, syncMode, coreBuilder.addressConverter)
        bitcoinCore = coreBuilder
                .setContext(context)
                .setExtendedKey(extendedKey)
                .setPurpose(HDWallet.Purpose.BIP44)
                .setNetwork(network)
                .setCheckpoint(checkpoint)
                .setPaymentAddressParser(paymentAddressParser)
                .setPeerSize(peerSize)
                .setSyncMode(syncMode)
                .setConfirmationThreshold(confirmationsThreshold)
                .setStorage(coreStorage)
                .setBlockHeaderHasher(X11Hasher())
                .setApiTransactionProvider(apiTransactionProvider)
                .setApiSyncStateManager(apiSyncStateManager)
                .setTransactionInfoConverter(dashTransactionInfoConverter)
                .setBlockValidator(blockValidatorSet)
                .setIsAnBaoWallet(isAnBaoWallet)
                .setIsSafe3Wallet(isSafe3Wallet)
                .addPlugin(hodlerPlugin)
                .build()
//                .setConnectionManager(connectionManager)
//                .addPlugin(HodlerPlugin(coreBuilder.addressConverter, coreStorage, BlockMedianTimeHelper(coreStorage)))
        bitcoinCore.listener = this

        //  extending bitcoinCore

        bitcoinCore.addMessageParser(MasternodeListDiffMessageParser())
                .addMessageParser(TransactionLockMessageParser())
                .addMessageParser(TransactionLockVoteMessageParser())
                .addMessageParser(ISLockMessageParser())

        bitcoinCore.addMessageSerializer(GetMasternodeListDiffMessageSerializer())

        val merkleRootHasher = MerkleRootHasher()
        val merkleRootCreator = MerkleRootCreator(merkleRootHasher)
        val masternodeListMerkleRootCalculator = MasternodeListMerkleRootCalculator(merkleRootCreator)
        val masternodeCbTxHasher = MasternodeCbTxHasher(CoinbaseTransactionSerializer(), merkleRootHasher)

        val quorumListManager = QuorumListManager(dashStorage, QuorumListMerkleRootCalculator(merkleRootCreator), QuorumSortedList())
        val masternodeListManager = MasternodeListManager(dashStorage, masternodeListMerkleRootCalculator, masternodeCbTxHasher, MerkleBranch(), MasternodeSortedList(), quorumListManager)
        val masternodeSyncer = MasternodeListSyncer(bitcoinCore, PeerTaskFactory(), masternodeListManager, bitcoinCore.initialDownload)

        bitcoinCore.addPeerTaskHandler(masternodeSyncer)
        bitcoinCore.addPeerSyncListener(masternodeSyncer)
        bitcoinCore.addPeerGroupListener(masternodeSyncer)

        val base58AddressConverter = Base58AddressConverter(network.addressVersion, network.addressScriptVersion)
//        bitcoinCore.addRestoreKeyConverter(Bip44RestoreKeyConverter(base58AddressConverter))

        //  extending bitcoinCore
        val bech32AddressConverter = SegwitAddressConverter(network.addressSegwitHrp)

        bitcoinCore.prependAddressConverter(bech32AddressConverter)

                bitcoinCore.addRestoreKeyConverter(Bip44RestoreKeyConverter(base58AddressConverter))
                bitcoinCore.addRestoreKeyConverter(hodlerPlugin)

                /*if (extendedKey != null) {
                    bitcoinCore.addRestoreKeyConverter(Bip49RestoreKeyConverter(base58AddressConverter))
                    bitcoinCore.addRestoreKeyConverter(Bip84RestoreKeyConverter(bech32AddressConverter))
                }*/


        val singleHasher = SingleSha256Hasher()
        val bls = BLS()
        val transactionLockVoteValidator = TransactionLockVoteValidator(dashStorage, singleHasher, bls)
        val instantSendLockValidator = InstantSendLockValidator(quorumListManager, bls)

        val transactionLockVoteManager = TransactionLockVoteManager(transactionLockVoteValidator)
        val instantSendLockManager = InstantSendLockManager(instantSendLockValidator)

        val instantSendLockHandler = InstantSendLockHandler(instantTransactionManager, instantSendLockManager)
        instantSendLockHandler.delegate = this
        val transactionLockVoteHandler = TransactionLockVoteHandler(instantTransactionManager, transactionLockVoteManager)
        transactionLockVoteHandler.delegate = this

        val instantSend = InstantSend(bitcoinCore.transactionSyncer, transactionLockVoteHandler, instantSendLockHandler)
        this.instantSend = instantSend

        bitcoinCore.addInventoryItemsHandler(instantSend)
        bitcoinCore.addPeerTaskHandler(instantSend)

        val calculator = TransactionSizeCalculator()
        val dustCalculator = DustCalculator(network.dustRelayTxFee, calculator)
        // ADD NEW OUTPUT PROVIDER FOR UNLOCKED
        confirmedUnlockedUnspentOutputProvider = ConfirmedUnlockedUnspentOutputProvider(coreStorage, confirmationsThreshold)
        bitcoinCore.prependUnspentOutputSelector(UnspentOutputSelector(calculator, dustCalculator, confirmedUnlockedUnspentOutputProvider))
        bitcoinCore.prependUnspentOutputSelector(UnspentOutputSelectorSingleNoChange(calculator, dustCalculator, confirmedUnlockedUnspentOutputProvider))
    }

    private fun hodlerPlugin(
            storage: Storage,
            syncMode: SyncMode,
            addressConverter: IAddressConverter
    ): HodlerPlugin {
        val blockMedianTimeHelper = BlockMedianTimeHelper(storage, approximate = syncMode is SyncMode.Blockchair)
        return HodlerPlugin(addressConverter, storage, blockMedianTimeHelper)
    }

    // BitcoinCore.Listener
    override fun onTransactionsUpdate(inserted: List<TransactionInfo>, updated: List<TransactionInfo>) {
        // check for all new transactions if it's has instant lock
        inserted.map { it.transactionHash.hexToByteArray().reversedArray() }.forEach {
            instantSend.handle(it)
        }

        listener?.onTransactionsUpdate(inserted.mapNotNull { it as? DashTransactionInfo }, updated.mapNotNull { it as? DashTransactionInfo })
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        listener?.onTransactionsDelete(hashes)
    }

    override fun onBalanceUpdate(balance: BalanceInfo) {
        listener?.onBalanceUpdate(balance)
    }

    override fun onLastBlockInfoUpdate(blockInfo: BlockInfo) {
        listener?.onLastBlockInfoUpdate(blockInfo)
    }

    override fun onKitStateUpdate(state: BitcoinCore.KitState) {
        try {
            listener?.onKitStateUpdate(state)
        } catch (e: Exception) {
        }
    }

    // IInstantTransactionDelegate
    override fun onUpdateInstant(transactionHash: ByteArray) {
        val transaction = dashStorage.getFullTransactionInfo(transactionHash) ?: return
        val transactionInfo = dashTransactionInfoConverter.transactionInfo(transaction)

        bitcoinCore.listenerExecutor.execute {
            listener?.onTransactionsUpdate(listOf(), listOf(transactionInfo))
        }
    }


    private fun apiTransactionProvider(
            networkType: NetworkType,
            syncMode: SyncMode,
            apiSyncStateManager: ApiSyncStateManager
    ) = when (networkType) {
        SafeKit.NetworkType.MainNet -> {
            val insightApiProvider = InsightApi("https://chain.anwang.org/insight-api-safe")

            if (syncMode is SyncMode.Blockchair) {
                val blockchairApi = BlockchairApi(network.blockchairChainId)
                val blockchairBlockHashFetcher = BlockchairBlockHashFetcher(blockchairApi)
                val blockchairProvider = BlockchairTransactionProvider(blockchairApi, blockchairBlockHashFetcher)

                BiApiTransactionProvider(
                        restoreProvider = insightApiProvider,
                        syncProvider = blockchairProvider,
                        syncStateManager = apiSyncStateManager
                )
            } else {
                insightApiProvider
            }
        }

        SafeKit.NetworkType.TestNet -> {
            InsightApi("http://dash-testnet.horizontalsystems.xyz/apg")
        }
    }

    companion object {
        const val maxTargetBits: Long = 0x1e0fffff

        const val targetSpacing = 30            // 0.5 min. for mining 1 Block
        const val targetTimespan = 90L          // 1 hour for 24 blocks
        const val heightInterval = targetTimespan / targetSpacing

        val defaultNetworkType: NetworkType = NetworkType.MainNet
        val defaultSyncMode: SyncMode = SyncMode.Api()
        const val defaultPeerSize: Int = 10
        const val defaultConfirmationsThreshold: Int = 6

        private fun getDatabaseNameCore(networkType: NetworkType, walletId: String, syncMode: SyncMode) =
                "${getDatabaseName(networkType, walletId, syncMode)}-core"

        private fun getDatabaseName(networkType: NetworkType, walletId: String, syncMode: SyncMode) =
                "Safe-${networkType.name}-$walletId-${syncMode.javaClass.simpleName}"

        private fun parseAddress(address: String, network: Network): Address {
            return Base58AddressConverter(network.addressVersion, network.addressScriptVersion).convert(address)
        }

        private fun network(networkType: DashKit.NetworkType) = when (networkType) {
            DashKit.NetworkType.MainNet -> MainNetDash()
            DashKit.NetworkType.TestNet -> TestNetDash()
        }

        fun clear(context: Context, networkType: NetworkType, walletId: String) {
            for (syncMode in listOf(SyncMode.Api(), SyncMode.Full(), SyncMode.Blockchair())) {
                try {
                    SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseNameCore(networkType, walletId, syncMode)))
                    SQLiteDatabase.deleteDatabase(context.getDatabasePath(getDatabaseName(networkType, walletId, syncMode)))
                } catch (ex: Exception) {
                    continue
                }
            }
        }
    }

}
