# Marketplace: Troubleshooting

## "Offer cannot be purchased"

Check:

- Are payment stacks and amounts correct?
- Has the daily limit been reached?
- Has the stock limit been reached?
- Is runtime state affected by restock/demand?

## "Marketplace does not open"

Check:

- Is the keybind assigned correctly?
- Do you have command permission?
- Does the server load Marketplace data without errors?

## "Changes are not visible"

Check:

- Was the change acknowledged server-side?
- Were viewers synchronized again?
- If JSON was edited manually: was reload run correctly?

## "JSON appears to be corrupted"

Check:

- Does the `.bak` file exist?
- Revert the most recent manual change
- Check server logs for parse/I/O errors
