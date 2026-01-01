package com.openkrieg

import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.formdev.flatlaf.FlatDarkLaf
import com.openkrieg.editor.constant.Constants
import com.openkrieg.editor.view.EditorView
import com.openkrieg.editor.viewmodel.EditorViewModel
import com.openkrieg.editor.viewmodel.internal.EditorType
import kotlinx.coroutines.runBlocking
import openkriegmapeditor.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import java.awt.Desktop
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.datatransfer.DataFlavor
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main() = application {

	try {
		runBlocking {
			val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()

			suspend fun register(path: String) {
				val fontBytes = Res.readBytes(path)
				val font = Font.createFont(Font.TRUETYPE_FONT, ByteArrayInputStream(fontBytes))
				ge.registerFont(font)
			}

			register("font/spectral_regular.ttf")
			register("font/spectral_medium.ttf")
			register("font/spectral_medium_italic.ttf")
		}
	} catch (e: Exception) {
		e.printStackTrace()
	}

	var themeStr by remember { mutableStateOf("dark") }
	FlatDarkLaf.setup()

	Window(
		onCloseRequest = ::exitApplication,
		title = "${Constants.NAME} Map Editor v${Constants.VERSION}",
		state = rememberWindowState(
			width = Constants.DEFAULT_WINDOW_WIDTH.dp,
			height = Constants.DEFAULT_WINDOW_HEIGHT.dp,
			position = WindowPosition.Aligned(Alignment.Center)
		),
		icon = painterResource(Res.drawable.app_icon)
	) {
		val editorViewModel by remember { mutableStateOf(EditorViewModel(window)) }
		Box(
			modifier = Modifier.fillMaxSize().fileDropTarget(editorViewModel, window)
		) {
			EditorView(editorViewModel).build()
		}
		MenuBar {
			Menu("File", mnemonic = 'F') {
				Menu("New...", mnemonic = 'N') {
					Item(
						"Project",
						icon = painterResource(Res.drawable.new),
						onClick = { editorViewModel.reset() },
						shortcut = KeyShortcut(Key.P, ctrl = true),
						enabled = editorViewModel.editorType != EditorType.NONE
					)
					Item(
						"Map...",
						icon = painterResource(Res.drawable.game_map),
						onClick = { editorViewModel.newMap() },
						shortcut = KeyShortcut(Key.M, ctrl = true),
						enabled = true
					)
					Item(
						"Palette",
						icon = painterResource(Res.drawable.palette),
						onClick = { editorViewModel.newPalette() },
						shortcut = KeyShortcut(Key.L, ctrl = true),
						enabled = true
					)
				}
				Item(
					"Open...", icon = painterResource(Res.drawable.open), onClick = { editorViewModel.promptOpenFile() }, shortcut = KeyShortcut(Key.O, ctrl = true)
				)
				Menu("Import...", mnemonic = 'I') {
					Item(
						"Base Image",
						icon = painterResource(Res.drawable.import_image),
						onClick = {
							if (editorViewModel.mapViewModel.openBaseImageOnly()) {
								editorViewModel.editorType = EditorType.RKM_MAP
							}
						},
						shortcut = KeyShortcut(Key.I, ctrl = true),
						enabled = editorViewModel.editorType == EditorType.NONE || editorViewModel.editorType == EditorType.RKM_MAP
					)
					Item(
						"Image Layers",
						icon = painterResource(Res.drawable.import_image_layers),
						onClick = {
							if (editorViewModel.mapViewModel.openImageLayers()) {
								editorViewModel.editorType = EditorType.RKM_MAP
							}
						},
						shortcut = KeyShortcut(Key.I, alt = true),
						enabled = editorViewModel.editorType == EditorType.NONE || editorViewModel.editorType == EditorType.RKM_MAP
					)
				}
				Separator()
				Item(
					"Save...",
					icon = painterResource(Res.drawable.save),
					onClick = { editorViewModel.save() },
					shortcut = KeyShortcut(Key.S, ctrl = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP || editorViewModel.editorType == EditorType.RKP_PALETTE
				)
				Separator()
				Item(
					"Exit", icon = painterResource(Res.drawable.exit), onClick = { exitProcess(0) }, shortcut = KeyShortcut(Key.E, ctrl = true)
				)
			}
			Menu("Edit", mnemonic = 'E') {
				Item(
					"Add as territory",
					icon = painterResource(Res.drawable.add_as_territory),
					onClick = { editorViewModel.mapViewModel.submitSelectedRegions(false) },
					shortcut = KeyShortcut(Key.F1, ctrl = false),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingRegion
				)
				Item(
					"Submit selected neighbors",
					icon = painterResource(Res.drawable.submit_selected_neighbors),
					onClick = { editorViewModel.mapViewModel.submitSelectedNeighbors() },
					shortcut = KeyShortcut(Key.F2, ctrl = false),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingTerritory
				)
				Separator()
				Item(
					"Delete selected territory",
					icon = painterResource(Res.drawable.delete_selected_territory),
					onClick = { editorViewModel.mapViewModel.deleteSelectedTerritory() },
					shortcut = KeyShortcut(Key.Delete, ctrl = false),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingTerritory
				)
				Item(
					"Delete all",
					icon = painterResource(Res.drawable.delete_all),
					onClick = { editorViewModel.mapViewModel.deleteAll() },
					shortcut = KeyShortcut(Key.Delete, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP
				)
				Separator()
				Item(
					"Generate neighbors for selected territory",
					icon = painterResource(Res.drawable.generate_neighbors),
					onClick = { editorViewModel.mapViewModel.generateNeighbors() },
					shortcut = KeyShortcut(Key.N, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP && editorViewModel.mapViewModel.isSelectingTerritory
				)
				Separator()
				Item(
					"Deselect",
					icon = painterResource(Res.drawable.deselect),
					onClick = { editorViewModel.mapViewModel.deselectAll() },
					shortcut = KeyShortcut(Key.D, ctrl = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP && (editorViewModel.mapViewModel.isSelectingRegion || editorViewModel.mapViewModel.isSelectingTerritory)
				)
			}
			Menu("Debug", mnemonic = 'D') {
				Item(
					"Import base image...",
					icon = painterResource(Res.drawable.reimport_base_image),
					onClick = { editorViewModel.mapViewModel.importBaseImage() },
					shortcut = KeyShortcut(Key.B, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP
				)
				Item(
					"Import text image...",
					icon = painterResource(Res.drawable.reimport_text_image),
					onClick = { editorViewModel.mapViewModel.importTextImage() },
					shortcut = KeyShortcut(Key.T, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP
				)
				Separator()
				Item(
					"Export base image...",
					icon = painterResource(Res.drawable.export_graph),
					onClick = { editorViewModel.mapViewModel.exportBaseImage() },
					shortcut = KeyShortcut(Key.V, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP // && baseImage != null
				)
				Item(
					"Export text image...",
					icon = painterResource(Res.drawable.export_graph),
					onClick = { editorViewModel.mapViewModel.exportTextImage() },
					shortcut = KeyShortcut(Key.Y, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP // && textImage != null
				)
				Separator()
				Item(
					"Import graph...",
					icon = painterResource(Res.drawable.import_graph),
					onClick = { editorViewModel.mapViewModel.importGraph() },
					shortcut = KeyShortcut(Key.G, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP
				)
				Item(
					"Export graph...",
					icon = painterResource(Res.drawable.export_graph),
					onClick = { editorViewModel.mapViewModel.exportGraph() },
					shortcut = KeyShortcut(Key.R, alt = true),
					enabled = editorViewModel.editorType == EditorType.RKM_MAP // && graph != null
				)
			}
			Menu("Help", mnemonic = 'H') {
				Item(
					"Discord", icon = painterResource(Res.drawable.discord), onClick = {
						openLink("https://discord.com/invite/8uWduVrUUa")
					})
				Separator()
				Item(
					"About", icon = painterResource(Res.drawable.about), onClick = {
						openLink("https://www.openkrieg.com")
					})
			}
		}
	}
}

private fun openLink(linkStr: String) {
	try {
		Desktop.getDesktop().browse(URI(linkStr))
	} catch (e: Exception) {
		// TODO: Open dialog popup?
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Modifier.fileDropTarget(
	model: EditorViewModel, window: ComposeWindow
): Modifier {
	val dropTarget = remember(model, window) {
		object : DragAndDropTarget {

			override fun onEntered(event: DragAndDropEvent) {
				if (model.editorType == EditorType.NONE) {
					model.isDragAndDropping = true
				}
			}

			override fun onExited(event: DragAndDropEvent) {
				if (model.editorType == EditorType.NONE) {
					model.isDragAndDropping = false
				}
			}

			override fun onEnded(event: DragAndDropEvent) {
				if (model.editorType == EditorType.NONE) {
					model.isDragAndDropping = false
				}
			}

			override fun onDrop(event: DragAndDropEvent): Boolean {
				model.isDragAndDropping = false

				if (model.editorType != EditorType.NONE) return false

				return try {
					// Access the native AWT transferable from the Compose event
					val transferable = event.awtTransferable

					if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						val droppedFiles = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

						if (droppedFiles.size == 1) {
							model.openFile(droppedFiles[0] as File)
							true
						} else {
							JOptionPane.showMessageDialog(window, "You can only drag in one file at a time.", "Error", JOptionPane.ERROR_MESSAGE)
							false
						}
					} else {
						false
					}
				} catch (e: Exception) {
					e.printStackTrace()
					JOptionPane.showMessageDialog(window, "Error opening file.", "Error", JOptionPane.ERROR_MESSAGE)
					false
				}
			}
		}
	}

	return this.dragAndDropTarget(
		shouldStartDragAndDrop = { event ->
			model.editorType == EditorType.NONE && event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
		}, target = dropTarget
	)
}