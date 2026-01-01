package com.openkrieg.editor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.openkrieg.editor.constant.Constants
import com.openkrieg.editor.util.ImageUtil
import com.riskrieg.codec.decode.RkmDecoder
import com.riskrieg.codec.decode.RkpDecoder
import com.riskrieg.codec.encode.RkpEncoder
import com.riskrieg.map.RkmMap
import com.riskrieg.map.Territory
import com.riskrieg.map.territory.Nucleus
import com.riskrieg.palette.RkpColor
import com.riskrieg.palette.RkpPalette
import io.github.aaronjyoder.fill.recursive.BlockFiller
import kotlinx.coroutines.*
import openkriegmapeditor.composeapp.generated.resources.Res
import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class PaletteViewModel(private val window: ComposeWindow, var mousePosition: Point) {

	private val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

	private var activeColor by mutableStateOf(RkpColor(-1, "None", 0, 0, 0))
	var newColorName by mutableStateOf("")
	var newColorHexString by mutableStateOf("")

	var paletteName by mutableStateOf("")
	var colorSet: TreeSet<RkpColor> by mutableStateOf(sortedSetOf())

	var paletteMap: RkmMap? by mutableStateOf(null)
		private set

	private var coloredBaseLayer: BufferedImage by mutableStateOf(BufferedImage(1, 1, 2))
	private var paintedTerritories: MutableMap<Territory, RkpColor> = HashMap()

	private val paletteNameRegex = Regex("[a-z0-9-]+")

	/** Methods **/

	init {
		reset()
	}

	fun init(palette: RkpPalette) {
		viewModelScope.launch {
			resetSuspend(loadDefaultPalette = false)

			paletteName = palette.name
			colorSet = TreeSet(palette.sortedColorSet)

		}
	}

	fun reset() {
		viewModelScope.launch {
			resetSuspend(loadDefaultPalette = true)
		}
	}

	private suspend fun resetSuspend(loadDefaultPalette: Boolean) {
		activeColor = RkpColor(-1, "None", 0, 0, 0)
		newColorName = ""
		newColorHexString = ""
		paintedTerritories.clear()

		try {
			val loadedMap = loadDefaultMap()
			paletteMap = loadedMap

			coloredBaseLayer = withContext(Dispatchers.IO) {
				ImageUtil.createCopy(loadedMap.baseLayer, BufferedImage.TYPE_INT_ARGB)
			}

			if (loadDefaultPalette) {
				val defaultPalette = loadDefaultPalette()
				paletteName = defaultPalette.name
				colorSet = TreeSet(defaultPalette.sortedColorSet)
			} else {
				paletteName = ""
				colorSet.clear()
			}

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun save() {
		if (colorSet.isEmpty()) {
			throw IllegalStateException("No colors to export.")
		}
		if (paletteName.isBlank()) {
			throw IllegalStateException("Please name your palette before saving.")
		}
		if (colorSet.size < RkpPalette.MINIMUM_SIZE) {
			throw IllegalStateException("Your palette must have at least ${RkpPalette.MINIMUM_SIZE} colors.")
		}
		if (colorSet.size > RkpPalette.MAXIMUM_SIZE) {
			throw IllegalStateException("Your palette can only have ${RkpPalette.MAXIMUM_SIZE} colors at maximum.")
		}

		val chooser = JFileChooser()
		chooser.dialogTitle = "Save ${Constants.NAME} Palette File"
		chooser.isAcceptAllFileFilterUsed = false
		chooser.fileFilter = FileNameExtensionFilter("${Constants.NAME} Palette File (*.rkp)", "rkp")
		chooser.currentDirectory = File(System.getProperty("user.home"))

		val normalizedName = Normalizer.normalize(paletteName, Normalizer.Form.NFD).replace("[^\\p{ASCII}]".toRegex(), "")
		val normalizedPaletteName = normalizedName.lowercase().replace("\\s+".toRegex(), "-").replace("[^a-z0-9-]".toRegex(), "")

		chooser.selectedFile = File("$normalizedPaletteName.rkp")
		if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
			if (chooser.selectedFile.name.isNullOrBlank() || !chooser.selectedFile.nameWithoutExtension.matches(paletteNameRegex)) {
				throw IllegalStateException("Invalid file name. Use only lowercase letters, numbers, and hyphens/dashes.")
			} else {
				val directory = chooser.currentDirectory.path.replace('\\', '/') + "/"
				try {
					val palette = RkpPalette(paletteName, colorSet)
					val encoder = RkpEncoder()
					val fos = FileOutputStream(File(directory + "${normalizedPaletteName}.rkp"))
					encoder.encode(palette, fos)
					fos.close()
					JOptionPane.showMessageDialog(window, "Palette file successfully exported to the selected directory.", "Success", JOptionPane.PLAIN_MESSAGE)
				} catch (e: Exception) {
					throw IllegalStateException("Unable to save palette file due to an unexpected error.")
				}
			}
		}
	}

	fun activeColorName(): String {
		return if (isActiveColorSelected()) activeColor.name else ""
	}

	fun activeColorHexString(): String {
		return if (isActiveColorSelected()) String.format("#%02X%02X%02X", activeColor.r, activeColor.g, activeColor.b) else ""
	}

	fun isNewColorNameValid(): Boolean {
		return newColorName.isNotBlank()
	}

	fun isNewColorHexStringValid(): Boolean {
		return try {
			newColorHexString.isNotBlank() && Color.decode(newColorHexString) != null
		} catch (e: Exception) {
			false
		}
	}

	fun colorSetContainsHexColor(): Boolean {
		for (gameColor in colorSet) {
			if (newColorHexString.trim() == String.format("#%02X%02X%02X", gameColor.r, gameColor.g, gameColor.b)) {
				return true
			}
		}
		return false
	}

	fun colorSetContainsNewColor(): Boolean {
		for (gameColor in colorSet) {
			if (newColorName.trim() == gameColor.name || newColorHexString.trim() == String.format("#%02X%02X%02X", gameColor.r, gameColor.g, gameColor.b)) {
				return true
			}
		}
		return false
	}

	fun selectActiveColor(rkpColor: RkpColor) {
		this.activeColor = rkpColor
	}

	fun deselectActiveColor() {
		this.activeColor = RkpColor(-1, "None", 0, 0, 0)
		this.newColorName = ""
		this.newColorHexString = ""
	}

	fun updateSelectedColor() { // TODO: Don't allow updating a color to one that already exists!
		this.newColorName = newColorName.trim()
		if (isActiveColorSelected() && isNewColorNameValid() && isNewColorHexStringValid()) {
			val awtColor = Color.decode(newColorHexString)
			val updatedColor = RkpColor(activeColor.order, newColorName, awtColor.red, awtColor.green, awtColor.blue)
			colorSet.remove(activeColor)
			colorSet.add(updatedColor)

			// Update relevant territories
			for (entry in paintedTerritories) {
				if (entry.value.order == updatedColor.order) {
					paintedTerritories[entry.key] = updatedColor
				}
			}

			// hack to update lazylist
			deselectActiveColor()
			selectActiveColor(updatedColor)
			newColorName = activeColorName()
			newColorHexString = activeColorHexString()

			updateMapImage()
		}
	}

	fun addNewColor() {
		this.newColorName = newColorName.trim()
		if (isNewColorNameValid() && isNewColorHexStringValid()) {
			val awtColor = Color.decode(newColorHexString)
			val newColor = RkpColor(colorSet.size, newColorName, awtColor.red, awtColor.green, awtColor.blue)
			colorSet.add(newColor)
		}
	}

	fun removeSelectedColor() {
		if (isActiveColorSelected()) {
			// Clear relevant territory first
			val territoryToClear: MutableSet<Territory> = mutableSetOf()
			for (entry in paintedTerritories) {
				if (entry.value == activeColor) {
					territoryToClear.add(entry.key)
				}
			}

			for (territory in territoryToClear) {
				paintedTerritories.remove(territory)
			}

			// Remove the color now
			val removedIndex = activeColor.order
			val oldToNewColorIndexMap: SortedMap<RkpColor, RkpColor> = sortedMapOf()
			for (i in removedIndex + 1 until colorSet.size) {
				val currentColor = getRkpColorAt(i)
				if (currentColor != null) {
					val newIndexColor = RkpColor(currentColor.order - 1, currentColor.name, currentColor.r, currentColor.g, currentColor.b)
					oldToNewColorIndexMap[currentColor] = newIndexColor
				}
			}

			// Update territory entries with reindexed colors
			for (colorEntry in oldToNewColorIndexMap) {
				for (territoryEntry in paintedTerritories) {
					if (territoryEntry.value == colorEntry.key) {
						paintedTerritories[territoryEntry.key] = colorEntry.value
					}
				}
			}

			colorSet.remove(activeColor)
			colorSet.removeAll(oldToNewColorIndexMap.keys)
			colorSet.addAll(oldToNewColorIndexMap.values)

			deselectActiveColor()
			updateMapImage()
		}
	}

	fun moveSelectedColorUp() {
		if (isActiveColorSelected()) {
			val activeColorMovedUp = RkpColor(activeColor.order - 1, activeColor.name, activeColor.r, activeColor.g, activeColor.b)
			val colorToMoveDown = getRkpColorAt(activeColorMovedUp.order)
			if (colorToMoveDown != null) {
				// Prep territories
				val activeColorTerritories = mutableListOf<Territory>()
				val toMoveDownColorTerritories = mutableListOf<Territory>()
				for (entry in paintedTerritories) {
					if (entry.value == activeColor) {
						activeColorTerritories.add(entry.key)
					} else if (entry.value == colorToMoveDown) {
						toMoveDownColorTerritories.add(entry.key)
					}
				}

				// Move colors
				val colorMovedDown = RkpColor(colorToMoveDown.order + 1, colorToMoveDown.name, colorToMoveDown.r, colorToMoveDown.g, colorToMoveDown.b)
				colorSet.remove(colorToMoveDown)
				colorSet.remove(activeColor)
				colorSet.add(activeColorMovedUp)
				colorSet.add(colorMovedDown)
				this.activeColor = activeColorMovedUp

				// Now update territories
				for (territory in activeColorTerritories) {
					paintedTerritories[territory] = activeColorMovedUp
				}
				for (territory in toMoveDownColorTerritories) {
					paintedTerritories[territory] = colorMovedDown
				}

			}
		}
	}

	fun moveSelectedColorDown() {
		if (isActiveColorSelected()) {
			val activeColorMovedDown = RkpColor(activeColor.order + 1, activeColor.name, activeColor.r, activeColor.g, activeColor.b)
			val colorToMoveUp = getRkpColorAt(activeColorMovedDown.order)
			if (colorToMoveUp != null) {
				// Prep territories
				val activeColorTerritories = mutableListOf<Territory>()
				val toMoveUpColorTerritories = mutableListOf<Territory>()
				for (entry in paintedTerritories) {
					if (entry.value == activeColor) {
						activeColorTerritories.add(entry.key)
					} else if (entry.value == colorToMoveUp) {
						toMoveUpColorTerritories.add(entry.key)
					}
				}


				// Move colors
				val colorMovedUp = RkpColor(colorToMoveUp.order - 1, colorToMoveUp.name, colorToMoveUp.r, colorToMoveUp.g, colorToMoveUp.b)
				colorSet.remove(colorToMoveUp)
				colorSet.remove(activeColor)
				colorSet.add(activeColorMovedDown)
				colorSet.add(colorMovedUp)
				this.activeColor = activeColorMovedDown

				// Now update territories
				for (territory in activeColorTerritories) {
					paintedTerritories[territory] = activeColorMovedDown
				}
				for (territory in toMoveUpColorTerritories) {
					paintedTerritories[territory] = colorMovedUp
				}

			}
		}
	}

	fun paletteMapColoredBaseLayer(): BufferedImage {
		return coloredBaseLayer
	}

	fun interact() {
		val map = paletteMap ?: return
		// checkAndCorrectMouseBounds()
		val selectedTerritory = getTerritory(map.baseLayer, mousePosition, map.vertices)
		if (selectedTerritory.isPresent) {
			if (isActiveColorSelected()) {
				paintedTerritories[selectedTerritory.get()] = activeColor
			} else {
				paintedTerritories.remove(selectedTerritory.get())
			}
			updateMapImage()
		}
	}

	private fun updateMapImage() {
		val map = paletteMap ?: return

		val copy = BufferedImage(map.baseLayer.width, map.baseLayer.height, BufferedImage.TYPE_INT_ARGB)
		val g2d = copy.createGraphics()
		g2d.drawImage(map.baseLayer, 0, 0, null)
		g2d.dispose()

		for (entry in paintedTerritories) {
			for (seedPoint in entry.key.nuclei()) {
				val point = seedPoint.toPoint()
				BlockFiller(copy).fill(point, entry.value.toAwtColor())
			}
		}
		coloredBaseLayer = copy
	}

	suspend fun loadDefaultPalette(): RkpPalette {
		val resource = loadResource("files/palette/default.rkp")
		return RkpDecoder().decode(resource)
	}

	suspend fun loadDefaultMap(): RkmMap {
		val resource = loadResource("files/map/north-america.rkm")
		return RkmDecoder().decode(resource)
	}

	private suspend fun loadResource(path: String): ByteArray {
		return Res.readBytes(path)
	}

	/* Private */

	private fun isActiveColorSelected(): Boolean {
		return this.activeColor != RkpColor(-1, "None", 0, 0, 0)
	}

	private fun getRkpColorWithOrder(order: Int): RkpColor? {
		for (rkpColor in colorSet) {
			if (order == rkpColor.order) {
				return rkpColor
			}
		}
		return null
	}

	private fun getRkpColorAt(index: Int): RkpColor? {
		if (index < 0) {
			return null
		} else if (index >= colorSet.size) {
			return null
		}
		return colorSet.toList()[index]
	}

	private fun getTerritory(baseLayer: BufferedImage, point: Point, territorySet: Set<Territory>): Optional<Territory> {
		val root = ImageUtil.getRootPixel(baseLayer, point)
		for (territory in territorySet) {
			if (territory.nuclei().contains(Nucleus(root.x, root.y))) {
				return Optional.of(territory)
			}
		}
		return Optional.empty()
	}

}