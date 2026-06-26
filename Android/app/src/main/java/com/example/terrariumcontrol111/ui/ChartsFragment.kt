package com.example.terrariumcontrol111.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.terrariumcontrol111.databinding.FragmentChartsBinding
import com.example.terrariumcontrol111.model.SensorData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.data.*
import java.text.SimpleDateFormat
import java.util.*

class ChartsFragment : Fragment() {

    private var _binding: FragmentChartsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupCharts()
        setupButtons()

        viewModel.sensorHistory.observe(viewLifecycleOwner) { data ->
            val sorted = data.sortedBy { it.timestamp ?: 0L }
            updateAllCharts(sorted)
        }

        viewModel.startSensorHistoryListener(1)
    }

    // ================= STYLE =================
    private fun setupCharts() {

        listOf(
            binding.chartSoilTemp,
            binding.chartSoilHumidity,
            binding.chartAirTemp
        ).forEach { chart ->

            chart.apply {
                setBackgroundColor(Color.parseColor("#121212"))
                setDrawGridBackground(true)
                setGridBackgroundColor(Color.parseColor("#1E1E1E"))

                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false

                axisLeft.textColor = Color.WHITE
                axisLeft.gridColor = Color.parseColor("#33FFFFFF")

                xAxis.apply {
                    position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                    textColor = Color.WHITE
                    setDrawGridLines(false)

                    granularity = 1f
                    setLabelCount(6, true)
                }

                setTouchEnabled(true)
                setScaleEnabled(false)
                setPinchZoom(false)
            }
        }
    }

    // ================= BUTTONS =================
    private fun setupButtons() {
        binding.btnToday.setOnClickListener { viewModel.startSensorHistoryListener(1) }
        binding.btn7days.setOnClickListener { viewModel.startSensorHistoryListener(7) }
        binding.btn30days.setOnClickListener { viewModel.startSensorHistoryListener(30) }
    }

    // ================= UPDATE =================
    private fun updateAllCharts(data: List<SensorData>) {

        updateChart(binding.chartSoilTemp, data, GraphType.SOIL_TEMP)
        updateChart(binding.chartSoilHumidity, data, GraphType.SOIL_HUMIDITY)
        updateChart(binding.chartAirTemp, data, GraphType.AIR_TEMP)

        binding.txtAirTemp.text = String.format("%.1f°C", data.lastOrNull()?.tempAir ?: 0f)
        binding.txtSoilTemp.text = String.format("%.1f°C", data.lastOrNull()?.tempSoil ?: 0f)
        binding.txtSoilHumidity.text = String.format("%.1f%%", data.lastOrNull()?.soilMoisture ?: 0f)
    }

    // ================= MAIN =================
    private fun updateChart(
        chart: LineChart,
        data: List<SensorData>,
        type: GraphType
    ) {

        val timestamps = data.map { it.timestamp ?: 0L }

        val entries = data.mapIndexed { index, sensor ->
            val yRaw = when (type) {
                GraphType.SOIL_TEMP -> sensor.tempSoil ?: 0f
                GraphType.SOIL_HUMIDITY -> sensor.soilMoisture ?: 0f
                GraphType.AIR_TEMP -> sensor.tempAir ?: 0f
            }

            Entry(index.toFloat(), yRaw) // ❗ без округлення тут
        }

        val color = when (type) {
            GraphType.SOIL_TEMP -> Color.parseColor("#FF7043")
            GraphType.SOIL_HUMIDITY -> Color.parseColor("#66BB6A")
            GraphType.AIR_TEMP -> Color.parseColor("#42A5F5")
        }

        val dataSet = LineDataSet(entries, "").apply {

            this.color = color
            setCircleColor(color)

            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)

            mode = LineDataSet.Mode.LINEAR

            setDrawFilled(true)
            fillColor = color
            fillAlpha = 20

            highLightColor = Color.WHITE
            highlightLineWidth = 0.6f
            setDrawHorizontalHighlightIndicator(false)
        }

        chart.data = LineData(dataSet)

        // ================= X AXIS =================
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val i = value.toInt()
                if (i !in timestamps.indices) return ""

                return SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(timestamps[i] * 1000))
            }
        }

        // ================= LEFT AXIS (тільки відображення) =================
        val axis = chart.axisLeft
        axis.removeAllLimitLines()

        axis.textColor = Color.WHITE
        axis.gridColor = Color.parseColor("#33FFFFFF")

        // ❗ ВАЖЛИВО: НЕ чіпаємо valueFormatter тут (це ламало лінії)
        axis.valueFormatter = null

        when (type) {

            GraphType.SOIL_TEMP -> {
                axis.axisMinimum = 12f
                axis.axisMaximum = 35f

                axis.addLimitLine(LimitLine(22f, "мін. норма").apply {
                    lineColor = Color.GREEN
                    textColor = Color.GREEN
                    lineWidth = 2f
                })

                axis.addLimitLine(LimitLine(28f, "макс. норма").apply {
                    lineColor = Color.GREEN
                    textColor = Color.GREEN
                    lineWidth = 2f
                })

                axis.addLimitLine(LimitLine(30f, "критично").apply {
                    lineColor = Color.RED
                    textColor = Color.RED
                    lineWidth = 2f
                })
            }

            GraphType.SOIL_HUMIDITY -> {
                axis.axisMinimum = 0f
                axis.axisMaximum = 100f

                axis.addLimitLine(LimitLine(60f, "норма").apply {
                    lineColor = Color.GREEN
                    textColor = Color.GREEN
                })

                axis.addLimitLine(LimitLine(30f, "критично").apply {
                    lineColor = Color.RED
                    textColor = Color.RED
                })
            }

            GraphType.AIR_TEMP -> {
                axis.axisMinimum = 12f
                axis.axisMaximum = 35f

                axis.addLimitLine(LimitLine(18f, "мін. норма").apply {
                    lineColor = Color.GREEN
                    textColor = Color.GREEN
                })

                axis.addLimitLine(LimitLine(28f, "макс. норма").apply {
                    lineColor = Color.GREEN
                    textColor = Color.GREEN
                })

                axis.addLimitLine(LimitLine(30f, "критично").apply {
                    lineColor = Color.RED
                    textColor = Color.RED
                })
            }
        }

        // ================= MARKER (ОЦЕ ТВОЯ "БІГАЮЧА ЛІНІЯ") =================
        chart.marker = CustomMarkerView(requireContext(), timestamps)

        chart.notifyDataSetChanged()
        chart.invalidate()
    }
}

enum class GraphType {
    SOIL_TEMP,
    SOIL_HUMIDITY,
    AIR_TEMP
}