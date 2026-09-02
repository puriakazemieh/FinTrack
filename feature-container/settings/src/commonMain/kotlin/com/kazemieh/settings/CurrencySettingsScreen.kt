package com.kazemieh.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazemieh.designsystem.LocalSpacing
import com.kazemieh.designsystem.component.FintrackBodyLargeText
import com.kazemieh.designsystem.component.FintrackBodyMediumText
import com.kazemieh.designsystem.component.FintrackLabelMediumText
import com.kazemieh.designsystem.component.FintrackOutlinedTextField
import com.kazemieh.designsystem.component.FintrackTitleLargeText
import com.kazemieh.designsystem.component.glass.FintrackScreen
import com.kazemieh.designsystem.component.glass.GlassCard
import com.kazemieh.designsystem.component.glass.Tabs
import com.kazemieh.money.Currency
import fintrack.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.kazemieh.common.toPersianDigits
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CurrencySettingsScreen(
    onBack: () -> Unit,
    viewModel: CurrencySettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val space = LocalSpacing.current

    val hasCustom = state.customCurrencies.isNotEmpty()
    val searching = state.searchQuery.isNotBlank()
    var selectedTab by remember { mutableStateOf(0) }

    val tabTitles = remember(hasCustom) {
        buildList {
            add(TabKind.FIAT)
            add(TabKind.CRYPTO)
            if (hasCustom) add(TabKind.CUSTOM)
        }
    }
    // Keep the index in range if the custom tab disappears, without mutating state mid-compose.
    val safeTab = selectedTab.coerceIn(0, tabTitles.lastIndex)

    val onSelect: (Currency) -> Unit = {
        viewModel.onIntent(CurrencySettingsIntent.SelectCurrency(it))
    }

    // While searching, show everything that matches across all groups; otherwise the
    // active tab's list — so reaching crypto is one tap, not a long scroll.
    val visibleList: List<Currency> = when {
        searching -> state.customCurrencies + state.fiatCurrencies + state.cryptoCurrencies
        tabTitles[safeTab] == TabKind.FIAT -> state.fiatCurrencies
        tabTitles[safeTab] == TabKind.CRYPTO -> state.cryptoCurrencies
        else -> state.customCurrencies
    }

    FintrackScreen(
        title = stringResource(Res.string.label_currency),
        onBack = onBack
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = space.large)) {
            FintrackOutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onIntent(CurrencySettingsIntent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = space.medium),
                label = { FintrackBodyLargeText(stringResource(Res.string.search_placeholder)) }
            )

            if (!searching) {
                Tabs(
                    tabs = tabTitles.map { stringResource(it.label) },
                    active = safeTab,
                    onChange = { selectedTab = it },
                    counts = tabTitles.map {
                        when (it) {
                            TabKind.FIAT -> state.fiatCurrencies.size
                            TabKind.CRYPTO -> state.cryptoCurrencies.size
                            TabKind.CUSTOM -> state.customCurrencies.size
                        }
                    },
                    modifier = Modifier.padding(bottom = space.medium)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(space.small),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(visibleList) { currency ->
                    CurrencyListItem(
                        currency = currency,
                        isSelected = currency.code == state.selectedCurrency.code,
                        onClick = { onSelect(currency) }
                    )
                }

                item {
                    AutoConvertCard()
                }
            }
        }
    }
}

private enum class TabKind(val label: org.jetbrains.compose.resources.StringResource) {
    FIAT(Res.string.currency_fiat),
    CRYPTO(Res.string.currency_crypto),
    CUSTOM(Res.string.label_management)
}

@Composable
fun SectionHeader(title: String) {
    val space = LocalSpacing.current
    FintrackTitleLargeText(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = space.small),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun CurrencyListItem(
    currency: Currency,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val displayName = getCurrencyDisplayName(currency)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) primaryColor.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    FintrackLabelMediumText(text = currency.code, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
            FintrackBodyMediumText(
                text = displayName,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            FintrackLabelMediumText(
                text = if (currency.code == "IRT") stringResource(Res.string.label_default) else currency.symbol,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = primaryColor)
            }
        }
    }
}

@Composable
fun getCurrencyDisplayName(currency: Currency): String {
    return when (currency.displayName) {
        "currency_name_irr" -> stringResource(Res.string.currency_name_irr)
        "currency_name_irt" -> stringResource(Res.string.currency_name_irt)
        "currency_name_usd" -> stringResource(Res.string.currency_name_usd)
        "currency_name_eur" -> stringResource(Res.string.currency_name_eur)
        "currency_name_gbp" -> stringResource(Res.string.currency_name_gbp)
        "currency_name_jpy" -> stringResource(Res.string.currency_name_jpy)
        "currency_name_cny" -> stringResource(Res.string.currency_name_cny)
        "currency_name_aud" -> stringResource(Res.string.currency_name_aud)
        "currency_name_cad" -> stringResource(Res.string.currency_name_cad)
        "currency_name_chf" -> stringResource(Res.string.currency_name_chf)
        "currency_name_aed" -> stringResource(Res.string.currency_name_aed)
        "currency_name_afn" -> stringResource(Res.string.currency_name_afn)
        "currency_name_all" -> stringResource(Res.string.currency_name_all)
        "currency_name_amd" -> stringResource(Res.string.currency_name_amd)
        "currency_name_ang" -> stringResource(Res.string.currency_name_ang)
        "currency_name_aoa" -> stringResource(Res.string.currency_name_aoa)
        "currency_name_ars" -> stringResource(Res.string.currency_name_ars)
        "currency_name_awg" -> stringResource(Res.string.currency_name_awg)
        "currency_name_azn" -> stringResource(Res.string.currency_name_azn)
        "currency_name_bam" -> stringResource(Res.string.currency_name_bam)
        "currency_name_bbd" -> stringResource(Res.string.currency_name_bbd)
        "currency_name_bdt" -> stringResource(Res.string.currency_name_bdt)
        "currency_name_bgn" -> stringResource(Res.string.currency_name_bgn)
        "currency_name_bhd" -> stringResource(Res.string.currency_name_bhd)
        "currency_name_bif" -> stringResource(Res.string.currency_name_bif)
        "currency_name_bmd" -> stringResource(Res.string.currency_name_bmd)
        "currency_name_bnd" -> stringResource(Res.string.currency_name_bnd)
        "currency_name_bob" -> stringResource(Res.string.currency_name_bob)
        "currency_name_brl" -> stringResource(Res.string.currency_name_brl)
        "currency_name_bsd" -> stringResource(Res.string.currency_name_bsd)
        "currency_name_btn" -> stringResource(Res.string.currency_name_btn)
        "currency_name_bwp" -> stringResource(Res.string.currency_name_bwp)
        "currency_name_byn" -> stringResource(Res.string.currency_name_byn)
        "currency_name_bzd" -> stringResource(Res.string.currency_name_bzd)
        "currency_name_cdf" -> stringResource(Res.string.currency_name_cdf)
        "currency_name_clp" -> stringResource(Res.string.currency_name_clp)
        "currency_name_cop" -> stringResource(Res.string.currency_name_cop)
        "currency_name_crc" -> stringResource(Res.string.currency_name_crc)
        "currency_name_cup" -> stringResource(Res.string.currency_name_cup)
        "currency_name_cve" -> stringResource(Res.string.currency_name_cve)
        "currency_name_czk" -> stringResource(Res.string.currency_name_czk)
        "currency_name_djf" -> stringResource(Res.string.currency_name_djf)
        "currency_name_dkk" -> stringResource(Res.string.currency_name_dkk)
        "currency_name_dop" -> stringResource(Res.string.currency_name_dop)
        "currency_name_dzd" -> stringResource(Res.string.currency_name_dzd)
        "currency_name_egp" -> stringResource(Res.string.currency_name_egp)
        "currency_name_ern" -> stringResource(Res.string.currency_name_ern)
        "currency_name_etb" -> stringResource(Res.string.currency_name_etb)
        "currency_name_fjd" -> stringResource(Res.string.currency_name_fjd)
        "currency_name_fkp" -> stringResource(Res.string.currency_name_fkp)
        "currency_name_gel" -> stringResource(Res.string.currency_name_gel)
        "currency_name_ghs" -> stringResource(Res.string.currency_name_ghs)
        "currency_name_gip" -> stringResource(Res.string.currency_name_gip)
        "currency_name_gmd" -> stringResource(Res.string.currency_name_gmd)
        "currency_name_gnf" -> stringResource(Res.string.currency_name_gnf)
        "currency_name_gtq" -> stringResource(Res.string.currency_name_gtq)
        "currency_name_gyd" -> stringResource(Res.string.currency_name_gyd)
        "currency_name_hkd" -> stringResource(Res.string.currency_name_hkd)
        "currency_name_hnl" -> stringResource(Res.string.currency_name_hnl)
        "currency_name_htg" -> stringResource(Res.string.currency_name_htg)
        "currency_name_huf" -> stringResource(Res.string.currency_name_huf)
        "currency_name_idr" -> stringResource(Res.string.currency_name_idr)
        "currency_name_ils" -> stringResource(Res.string.currency_name_ils)
        "currency_name_inr" -> stringResource(Res.string.currency_name_inr)
        "currency_name_iqd" -> stringResource(Res.string.currency_name_iqd)
        "currency_name_isk" -> stringResource(Res.string.currency_name_isk)
        "currency_name_jmd" -> stringResource(Res.string.currency_name_jmd)
        "currency_name_jod" -> stringResource(Res.string.currency_name_jod)
        "currency_name_kes" -> stringResource(Res.string.currency_name_kes)
        "currency_name_kgs" -> stringResource(Res.string.currency_name_kgs)
        "currency_name_khr" -> stringResource(Res.string.currency_name_khr)
        "currency_name_kmf" -> stringResource(Res.string.currency_name_kmf)
        "currency_name_kpw" -> stringResource(Res.string.currency_name_kpw)
        "currency_name_krw" -> stringResource(Res.string.currency_name_krw)
        "currency_name_kwd" -> stringResource(Res.string.currency_name_kwd)
        "currency_name_kyd" -> stringResource(Res.string.currency_name_kyd)
        "currency_name_kzt" -> stringResource(Res.string.currency_name_kzt)
        "currency_name_lak" -> stringResource(Res.string.currency_name_lak)
        "currency_name_lbp" -> stringResource(Res.string.currency_name_lbp)
        "currency_name_lkr" -> stringResource(Res.string.currency_name_lkr)
        "currency_name_lrd" -> stringResource(Res.string.currency_name_lrd)
        "currency_name_lsl" -> stringResource(Res.string.currency_name_lsl)
        "currency_name_lyd" -> stringResource(Res.string.currency_name_lyd)
        "currency_name_mad" -> stringResource(Res.string.currency_name_mad)
        "currency_name_mdl" -> stringResource(Res.string.currency_name_mdl)
        "currency_name_mga" -> stringResource(Res.string.currency_name_mga)
        "currency_name_mkd" -> stringResource(Res.string.currency_name_mkd)
        "currency_name_mmk" -> stringResource(Res.string.currency_name_mmk)
        "currency_name_mnt" -> stringResource(Res.string.currency_name_mnt)
        "currency_name_mop" -> stringResource(Res.string.currency_name_mop)
        "currency_name_mru" -> stringResource(Res.string.currency_name_mru)
        "currency_name_mur" -> stringResource(Res.string.currency_name_mur)
        "currency_name_mvr" -> stringResource(Res.string.currency_name_mvr)
        "currency_name_mwk" -> stringResource(Res.string.currency_name_mwk)
        "currency_name_mxn" -> stringResource(Res.string.currency_name_mxn)
        "currency_name_myr" -> stringResource(Res.string.currency_name_myr)
        "currency_name_mzn" -> stringResource(Res.string.currency_name_mzn)
        "currency_name_nad" -> stringResource(Res.string.currency_name_nad)
        "currency_name_ngn" -> stringResource(Res.string.currency_name_ngn)
        "currency_name_nio" -> stringResource(Res.string.currency_name_nio)
        "currency_name_nok" -> stringResource(Res.string.currency_name_nok)
        "currency_name_npr" -> stringResource(Res.string.currency_name_npr)
        "currency_name_nzd" -> stringResource(Res.string.currency_name_nzd)
        "currency_name_omr" -> stringResource(Res.string.currency_name_omr)
        "currency_name_pab" -> stringResource(Res.string.currency_name_pab)
        "currency_name_pen" -> stringResource(Res.string.currency_name_pen)
        "currency_name_pgk" -> stringResource(Res.string.currency_name_pgk)
        "currency_name_php" -> stringResource(Res.string.currency_name_php)
        "currency_name_pkr" -> stringResource(Res.string.currency_name_pkr)
        "currency_name_pln" -> stringResource(Res.string.currency_name_pln)
        "currency_name_pyg" -> stringResource(Res.string.currency_name_pyg)
        "currency_name_qar" -> stringResource(Res.string.currency_name_qar)
        "currency_name_ron" -> stringResource(Res.string.currency_name_ron)
        "currency_name_rsd" -> stringResource(Res.string.currency_name_rsd)
        "currency_name_rub" -> stringResource(Res.string.currency_name_rub)
        "currency_name_rwf" -> stringResource(Res.string.currency_name_rwf)
        "currency_name_sar" -> stringResource(Res.string.currency_name_sar)
        "currency_name_sbd" -> stringResource(Res.string.currency_name_sbd)
        "currency_name_scr" -> stringResource(Res.string.currency_name_scr)
        "currency_name_sdg" -> stringResource(Res.string.currency_name_sdg)
        "currency_name_sek" -> stringResource(Res.string.currency_name_sek)
        "currency_name_sgd" -> stringResource(Res.string.currency_name_sgd)
        "currency_name_shp" -> stringResource(Res.string.currency_name_shp)
        "currency_name_sle" -> stringResource(Res.string.currency_name_sle)
        "currency_name_sos" -> stringResource(Res.string.currency_name_sos)
        "currency_name_srd" -> stringResource(Res.string.currency_name_srd)
        "currency_name_ssp" -> stringResource(Res.string.currency_name_ssp)
        "currency_name_stn" -> stringResource(Res.string.currency_name_stn)
        "currency_name_svc" -> stringResource(Res.string.currency_name_svc)
        "currency_name_syp" -> stringResource(Res.string.currency_name_syp)
        "currency_name_szl" -> stringResource(Res.string.currency_name_szl)
        "currency_name_thb" -> stringResource(Res.string.currency_name_thb)
        "currency_name_tjs" -> stringResource(Res.string.currency_name_tjs)
        "currency_name_tmt" -> stringResource(Res.string.currency_name_tmt)
        "currency_name_tnd" -> stringResource(Res.string.currency_name_tnd)
        "currency_name_top" -> stringResource(Res.string.currency_name_top)
        "currency_name_try" -> stringResource(Res.string.currency_name_try)
        "currency_name_ttd" -> stringResource(Res.string.currency_name_ttd)
        "currency_name_twd" -> stringResource(Res.string.currency_name_twd)
        "currency_name_tzs" -> stringResource(Res.string.currency_name_tzs)
        "currency_name_uah" -> stringResource(Res.string.currency_name_uah)
        "currency_name_ugx" -> stringResource(Res.string.currency_name_ugx)
        "currency_name_uyu" -> stringResource(Res.string.currency_name_uyu)
        "currency_name_uzs" -> stringResource(Res.string.currency_name_uzs)
        "currency_name_ves" -> stringResource(Res.string.currency_name_ves)
        "currency_name_vnd" -> stringResource(Res.string.currency_name_vnd)
        "currency_name_vuv" -> stringResource(Res.string.currency_name_vuv)
        "currency_name_wst" -> stringResource(Res.string.currency_name_wst)
        "currency_name_xaf" -> stringResource(Res.string.currency_name_xaf)
        "currency_name_xcd" -> stringResource(Res.string.currency_name_xcd)
        "currency_name_xof" -> stringResource(Res.string.currency_name_xof)
        "currency_name_xpf" -> stringResource(Res.string.currency_name_xpf)
        "currency_name_yer" -> stringResource(Res.string.currency_name_yer)
        "currency_name_zar" -> stringResource(Res.string.currency_name_zar)
        "currency_name_zmw" -> stringResource(Res.string.currency_name_zmw)
        "crypto_name_btc" -> stringResource(Res.string.crypto_name_btc)
        "crypto_name_eth" -> stringResource(Res.string.crypto_name_eth)
        "crypto_name_usdt" -> stringResource(Res.string.crypto_name_usdt)
        "crypto_name_bnb" -> stringResource(Res.string.crypto_name_bnb)
        "crypto_name_usdc" -> stringResource(Res.string.crypto_name_usdc)
        "crypto_name_xrp" -> stringResource(Res.string.crypto_name_xrp)
        "crypto_name_sol" -> stringResource(Res.string.crypto_name_sol)
        "crypto_name_trx" -> stringResource(Res.string.crypto_name_trx)
        "crypto_name_hype" -> stringResource(Res.string.crypto_name_hype)
        "crypto_name_doge" -> stringResource(Res.string.crypto_name_doge)
        "crypto_name_leo" -> stringResource(Res.string.crypto_name_leo)
        "crypto_name_zec" -> stringResource(Res.string.crypto_name_zec)
        "crypto_name_xlm" -> stringResource(Res.string.crypto_name_xlm)
        "crypto_name_xmr" -> stringResource(Res.string.crypto_name_xmr)
        "crypto_name_ada" -> stringResource(Res.string.crypto_name_ada)
        "crypto_name_link" -> stringResource(Res.string.crypto_name_link)
        "crypto_name_near" -> stringResource(Res.string.crypto_name_near)
        "crypto_name_ton" -> stringResource(Res.string.crypto_name_ton)
        "crypto_name_dot" -> stringResource(Res.string.crypto_name_dot)
        "crypto_name_shib" -> stringResource(Res.string.crypto_name_shib)
        "crypto_name_avax" -> stringResource(Res.string.crypto_name_avax)
        "crypto_name_stx" -> stringResource(Res.string.crypto_name_stx)
        "crypto_name_wbtc" -> stringResource(Res.string.crypto_name_wbtc)
        "crypto_name_dai" -> stringResource(Res.string.crypto_name_dai)
        "crypto_name_uni" -> stringResource(Res.string.crypto_name_uni)
        "crypto_name_ltc" -> stringResource(Res.string.crypto_name_ltc)
        "crypto_name_pol" -> stringResource(Res.string.crypto_name_pol)
        "crypto_name_bch" -> stringResource(Res.string.crypto_name_bch)
        "crypto_name_icp" -> stringResource(Res.string.crypto_name_icp)
        "crypto_name_apt" -> stringResource(Res.string.crypto_name_apt)
        "crypto_name_rndr" -> stringResource(Res.string.crypto_name_rndr)
        "crypto_name_hbar" -> stringResource(Res.string.crypto_name_hbar)
        "crypto_name_arb" -> stringResource(Res.string.crypto_name_arb)
        "crypto_name_kas" -> stringResource(Res.string.crypto_name_kas)
        "crypto_name_fil" -> stringResource(Res.string.crypto_name_fil)
        "crypto_name_imx" -> stringResource(Res.string.crypto_name_imx)
        "crypto_name_okb" -> stringResource(Res.string.crypto_name_okb)
        "crypto_name_mnt" -> stringResource(Res.string.crypto_name_mnt)
        "crypto_name_op" -> stringResource(Res.string.crypto_name_op)
        "crypto_name_vet" -> stringResource(Res.string.crypto_name_vet)
        "crypto_name_ldo" -> stringResource(Res.string.crypto_name_ldo)
        "crypto_name_grt" -> stringResource(Res.string.crypto_name_grt)
        "crypto_name_rune" -> stringResource(Res.string.crypto_name_rune)
        "crypto_name_theta" -> stringResource(Res.string.crypto_name_theta)
        "crypto_name_inj" -> stringResource(Res.string.crypto_name_inj)
        "crypto_name_tia" -> stringResource(Res.string.crypto_name_tia)
        "crypto_name_tao" -> stringResource(Res.string.crypto_name_tao)
        "crypto_name_ar" -> stringResource(Res.string.crypto_name_ar)
        "crypto_name_bgb" -> stringResource(Res.string.crypto_name_bgb)
        else -> currency.displayName
    }
}

@Composable
fun AutoConvertCard() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GlassCard(
            modifier = Modifier.padding(top = 8.dp),
            padding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Public,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FintrackBodyLargeText(
                        text = stringResource(Res.string.label_auto_convert),
                        fontWeight = FontWeight.SemiBold
                    )
                    FintrackBodyMediumText(
                        text = stringResource(Res.string.label_auto_convert_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
