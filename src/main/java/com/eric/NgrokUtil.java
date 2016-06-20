package com.eric;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NgrokUtil extends Command {

	@Parameter(hidden = true, names = "--api", description = "The api url to use when connecting to the ngrok process.")
	private String api = "http://localhost:4040/api/tunnels";

	@Parameter(names = "--http", description = "When set, get the unsecured http:// address rather than https:// (default)")
	private boolean http;

	@Parameter(names = { "-o", "--open" }, description = "Open the url with the default application.")
	private boolean openUrl;

	@Parameter(names = { "-c", "--copy" }, description = "Copies the url to the clipboard.")
	private boolean copyToClipboard;

	@Parameter(names = { "-p", "--path" }, description = "Append this path to the endpoint url.")
	private String path;

	@Override
	protected String getProgramName() {
		return "ngrok-util";
	}

	@Override
	protected void run() throws Exception {
		// call api and get tunnel info
		String url = null;
		String contents = IOUtils.toString(new URL(api), StandardCharsets.UTF_8);
		JsonObject obj = new Gson().fromJson(contents, JsonObject.class);
		for (JsonElement ele : obj.getAsJsonArray("tunnels")) {
			if (ele.getAsJsonObject().get("proto").getAsString().equals(http ? "http" : "https")) {
				url = ele.getAsJsonObject().get("public_url").getAsString();
				break;
			}
		}

		if (url != null) {
			if (path != null) {
				url += path;
			}

			if (copyToClipboard) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
			}

			if (openUrl) {
				Desktop.getDesktop().browse(new URI(url));
			}

			out(url);
		} else {
			err("No ngrok tunnel address was found.");
		}
	}

	public static void main(String[] args) {
		Command.main(new NgrokUtil(), args);
	}
}
