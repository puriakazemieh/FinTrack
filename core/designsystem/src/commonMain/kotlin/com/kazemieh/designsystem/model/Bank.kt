package com.kazemieh.designsystem.model

import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class BankType { Bank, NeoBank, CreditInstitution }

enum class AccountType(val title: StringResource) {
    ShortTermDeposit(Res.string.short_term_deposit)
}

enum class Bank(
    val type: BankType = BankType.Bank,
    val icon: DrawableResource,
    val logo: DrawableResource,
    val title: StringResource,
    val isNeo: Boolean = false,
    val isMerged: Boolean = false,
    val prefixes: List<String> = emptyList(),
    val nonPrefixes: List<String> = emptyList(),
    val supportedAccountTypes: List<AccountType> = AccountType.entries
) {
    ABank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_abank,
        logo = Res.drawable.ic_bank_abank_logo,
        title = Res.string.bank_abank,
        isNeo = true,
        prefixes = listOf(),
    ),
    Baam(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_baam,
        logo = Res.drawable.ic_bank_baam,
        title = Res.string.bank_baam,
        isNeo = true,
    ),
    Banket(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_banket,
        logo = Res.drawable.ic_bank_banket,
        title = Res.string.bank_banket,
        isNeo = true,
        prefixes = listOf(),
    ),
    Bankino(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_bankino,
        logo = Res.drawable.ic_bank_bankino_logo,
        title = Res.string.bank_bankino,
        isNeo = true,
        prefixes = listOf("58594710"),
        supportedAccountTypes = listOf(AccountType.ShortTermDeposit)
    ),
    Bankvareh(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_bankvareh,
        logo = Res.drawable.ic_bank_bankvareh,
        title = Res.string.bank_bankvareh,
        isNeo = true,
        prefixes = listOf(),
    ),
    Bajet(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_bajet,
        logo = Res.drawable.ic_bank_bajet_logo,
        title = Res.string.bank_bajet,
        isNeo = true,
    ),
    Baran(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_baran,
        logo = Res.drawable.ic_bank_baran_logo,
        title = Res.string.bank_baran,
        isNeo = true,
    ),
    BluBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_blu,
        logo = Res.drawable.ic_bank_blu_logo,
        title = Res.string.bank_blu,
        isNeo = true,
        prefixes = listOf("62198618", "62198619"),
        supportedAccountTypes = listOf(AccountType.ShortTermDeposit)
    ),
    BluJunior(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_blu_jr,
        logo = Res.drawable.ic_bank_blu_jr_logo,
        title = Res.string.bank_blu_jr,
        isNeo = true,
        supportedAccountTypes = listOf(AccountType.ShortTermDeposit)
    ),
    GreenBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_greenbank,
        logo = Res.drawable.ic_bank_greenbank_logo,
        title = Res.string.bank_greenbank,
        isNeo = true,
        prefixes = listOf(),
    ),
    HiBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_hibank,
        logo = Res.drawable.ic_bank_hibank_logo,
        title = Res.string.bank_hibank,
        isNeo = true,
        prefixes = listOf(),
    ),
    MegaBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_megabank,
        logo = Res.drawable.ic_bank_megabank,
        title = Res.string.bank_megabank,
        isNeo = true,
        prefixes = listOf(),
    ),
    MetaBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_metabank,
        logo = Res.drawable.ic_bank_metabank_logo,
        title = Res.string.bank_metabank,
        isNeo = true,
        prefixes = listOf(),
    ),
    OmidBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_omid,
        logo = Res.drawable.ic_bank_omid_logo,
        title = Res.string.bank_omidbank,
        isNeo = true
    ),
    QBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_qbank,
        logo = Res.drawable.ic_bank_qbank_logo,
        title = Res.string.bank_qbank,
        isNeo = true,
    ),
    Sepino(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_sepino,
        logo = Res.drawable.ic_bank_sepino_logo,
        title = Res.string.bank_sepino,
        isNeo = true,
    ),
    Sibank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_sibank,
        logo = Res.drawable.ic_bank_sibank,
        title = Res.string.bank_sibank,
        isNeo = true,
    ),
    ToBank(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_tobank,
        logo = Res.drawable.ic_bank_tobank_logo,
        title = Res.string.bank_tobank,
        isNeo = true,
    ),
    Wepod(
        type = BankType.NeoBank,
        icon = Res.drawable.ic_bank_wepod,
        logo = Res.drawable.ic_bank_wepod_logo,
        title = Res.string.bank_wepod,
        isNeo = true,
        prefixes = listOf("50222915"),
    ),
    Ansar(
        icon = Res.drawable.ic_bank_ansar,
        logo = Res.drawable.ic_bank_ansar,
        title = Res.string.bank_ansar,
        isMerged = true
    ),
    Ayandeh(
        icon = Res.drawable.ic_bank_ayandeh,
        logo = Res.drawable.ic_bank_ayandeh_logo,
        title = Res.string.bank_ayandeh,
        isMerged = true
    ),
    Central(
        icon = Res.drawable.ic_bank_central,
        logo = Res.drawable.ic_bank_central_logo,
        title = Res.string.bank_central,
        prefixes = listOf("636795")
    ),
    Day(
        icon = Res.drawable.ic_bank_day,
        logo = Res.drawable.ic_bank_day_logo,
        title = Res.string.bank_day,
        prefixes = listOf("502938")
    ),
    EghtesadNovin(
        icon = Res.drawable.ic_bank_eghtesad_novin,
        logo = Res.drawable.ic_bank_eghtesad_novin_logo,
        title = Res.string.bank_eghtesad_novin,
        prefixes = listOf("627412")
    ),
    Gardeshgari(
        icon = Res.drawable.ic_bank_gardeshgari,
        logo = Res.drawable.ic_bank_gardeshgari_logo,
        title = Res.string.bank_gardeshgari,
        prefixes = listOf("505416")
    ),
    IranVenezuelaBiNationalBank(
        icon = Res.drawable.ic_bank_iran_venezuela,
        logo = Res.drawable.ic_bank_iran_venezuela_logo,
        title = Res.string.bank_iran_venezuela,
        prefixes = listOf("581874")
    ),
    IranZamin(
        icon = Res.drawable.ic_bank_iranzamin,
        logo = Res.drawable.ic_bank_iranzamin_logo,
        title = Res.string.bank_iranzamin,
        prefixes = listOf("505785")
    ),
    Karafarin(
        icon = Res.drawable.ic_bank_karafarin,
        logo = Res.drawable.ic_bank_karafarin_logo,
        title = Res.string.bank_karafarin,
        prefixes = listOf("502910", "627488")
    ),
    Keshavarzi(
        icon = Res.drawable.ic_bank_keshavarzi,
        logo = Res.drawable.ic_bank_keshavarzi_logo,
        title = Res.string.bank_keshavarzi,
        prefixes = listOf("603770", "639217")
    ),
    Khavarmianeh(
        icon = Res.drawable.ic_bank_khavarmianeh,
        logo = Res.drawable.ic_bank_khavarmianeh_logo,
        title = Res.string.bank_khavarmianeh,
        prefixes = listOf("585947"),
        nonPrefixes = listOf("58594710")
    ),
    Maskan(
        icon = Res.drawable.ic_bank_maskan,
        logo = Res.drawable.ic_bank_maskan_logo,
        title = Res.string.bank_maskan,
        prefixes = listOf("628023")
    ),
    Mehr(
        icon = Res.drawable.ic_bank_mehr,
        logo = Res.drawable.ic_bank_mehr_logo,
        title = Res.string.bank_mehr,
        prefixes = listOf("606373")
    ),
    Melal(
        type = BankType.CreditInstitution,
        icon = Res.drawable.ic_bank_melal,
        logo = Res.drawable.ic_bank_melal_logo,
        title = Res.string.bank_melal,
        prefixes = listOf("606256")
    ),
    Mellat(
        icon = Res.drawable.ic_bank_mellat,
        logo = Res.drawable.ic_bank_mellat_logo,
        title = Res.string.bank_mellat,
        prefixes = listOf("610433", "991975")
    ),
    Melli(
        icon = Res.drawable.ic_bank_melli,
        logo = Res.drawable.ic_bank_melli_logo,
        title = Res.string.bank_melli,
        prefixes = listOf("603799", "507677", "636214")
    ),
    Noor(
        type = BankType.CreditInstitution,
        icon = Res.drawable.ic_bank_noor,
        logo = Res.drawable.ic_bank_noor_logo,
        title = Res.string.bank_noor,
        isMerged = true
    ),
    Parsian(
        icon = Res.drawable.ic_bank_parsian,
        logo = Res.drawable.ic_bank_parsian_logo,
        title = Res.string.bank_parsian,
        prefixes = listOf("622106", "627884", "639194")
    ),
    Pasargad(
        icon = Res.drawable.ic_bank_pasargad,
        logo = Res.drawable.ic_bank_pasargad_logo,
        title = Res.string.bank_pasargad,
        prefixes = listOf("502229", "639347"),
        nonPrefixes = listOf("50222915")
    ),
    PostBank(
        icon = Res.drawable.ic_bank_postbank,
        logo = Res.drawable.ic_bank_postbank_logo,
        title = Res.string.bank_postbank,
        prefixes = listOf("627760")
    ),
    Refah(
        icon = Res.drawable.ic_bank_refah,
        logo = Res.drawable.ic_bank_refah_logo,
        title = Res.string.bank_refah,
        prefixes = listOf("589463")
    ),
    Resalat(
        icon = Res.drawable.ic_bank_resalat_icon,
        logo = Res.drawable.ic_bank_resalat_logo,
        title = Res.string.bank_resalat,
        prefixes = listOf("504172")
    ),
    Saderat(
        icon = Res.drawable.ic_bank_saderat,
        logo = Res.drawable.ic_bank_saderat_logo,
        title = Res.string.bank_saderat,
        prefixes = listOf("603769"),
    ),
    Saman(
        icon = Res.drawable.ic_bank_saman,
        logo = Res.drawable.ic_bank_saman_logo,
        title = Res.string.bank_saman,
        prefixes = listOf("621986"),
        nonPrefixes = listOf("62198618", "62198619")
    ),
    SanatVaMaadan(
        icon = Res.drawable.ic_bank_sanat_maadan,
        logo = Res.drawable.ic_bank_sanat_maadan_logo,
        title = Res.string.bank_sanat_maadan,
        prefixes = listOf("627961")
    ),
    Sarmayeh(
        icon = Res.drawable.ic_bank_sarmayeh,
        logo = Res.drawable.ic_bank_sarmayeh_logo,
        title = Res.string.bank_sarmayeh,
        prefixes = listOf("639607")
    ),
    Sepah(
        icon = Res.drawable.ic_bank_sepah,
        logo = Res.drawable.ic_bank_sepah_logo,
        title = Res.string.bank_sepah,
        prefixes = listOf("589210", "627381", "636949", "639599", "639370")
    ),
    Shahr(
        icon = Res.drawable.ic_bank_shahr,
        logo = Res.drawable.ic_bank_shahr_logo,
        title = Res.string.bank_shahr,
        prefixes = listOf("502806", "504706")
    ),
    Sina(
        icon = Res.drawable.ic_bank_sina,
        logo = Res.drawable.ic_bank_sina_logo,
        title = Res.string.bank_sina,
        prefixes = listOf("639346")
    ),
    Tejarat(
        icon = Res.drawable.ic_bank_tejarat,
        logo = Res.drawable.ic_bank_tejarat_logo,
        title = Res.string.bank_tejarat,
        prefixes = listOf("585983", "627353")
    ),
    ToseSaderat(
        icon = Res.drawable.ic_bank_tose_saderat,
        logo = Res.drawable.ic_bank_tose_saderat_logo,
        title = Res.string.bank_tose_saderat,
        prefixes = listOf("627648")
    ),
    ToseTaavon(
        icon = Res.drawable.ic_bank_tose_taavon,
        logo = Res.drawable.ic_bank_tose_taavon_logo,
        title = Res.string.bank_tose_taavon,
        prefixes = listOf("502908")
    ),
    Unknown(
        icon = Res.drawable.ic_bank_unknown,
        logo = Res.drawable.ic_bank_unknown,
        title = Res.string.bank_unknown,
    );

    companion object {
        val allBanks = entries.filter { it != Unknown }

        fun fromCardNumber(cardNumber: String): Bank {
            val cardPrefix = cardNumber.take(8)

            for (bank in allBanks) {
                if (bank.prefixes.any { cardPrefix.startsWith(it) }) {
                    return bank
                }
            }

            val fallbackPrefix = cardNumber.take(6)
            for (bank in allBanks) {
                if (
                    bank.prefixes.any { fallbackPrefix.startsWith(it) } &&
                    bank.nonPrefixes.none { cardPrefix.startsWith(it) }
                ) {
                    return bank
                }
            }

            return Unknown
        }
    }
}
