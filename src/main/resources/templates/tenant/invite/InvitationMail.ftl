<!DOCTYPE HTML>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>You are invited to "${tenantName}" tenant</title>
</head>
<body style="font-family: Segoe UI, Helvetica Neue, Arial, sans-serif;">
<table border="0" cellpadding="0" cellspacing="0" width="600">
    <thead>
    <tr>
        <td width="200"></td>
        <td width="200"></td>
        <td width="200"></td>
    </tr>
    </thead>
    <tr>
        <td colspan="3" >
            <img src="${logo}" alt="Sigil" width="80">
        </td>
    </tr>
    <tr>
        <td colspan="3">
            <h3 style="font-weight: 600;">
                ${greeting},
            </h3>
            <p style="margin-top: 0;">
                You have been invited to join the&nbsp;
                <span style="font-weight: 600;">
                    ${tenantName}
                </span>
                &nbsp;tenant on Sigil. To accept the invitation, click the link below:
            </p>
        </td>
    </tr>
    <tr>
        <td></td>
        <td style="display: block">
            <a href="${completionUri}" style="box-sizing: border-box; display: inline-block; text-decoration: none; -webkit-text-size-adjust: none; text-align: center;  border-radius: 4px; -webkit-border-radius: 4px;  -moz-border-radius: 4px;  width:100%;  max-width:100%;  overflow-wrap: break-word;  word-break: break-word;  word-wrap:break-word;  mso-border-alt: none; font-size: 14px;">
                <span style="display:block;padding:10px 20px;line-height:120%; color: white; background-color: black;">
                    Accept invitation
                </span>
            </a>
        </td>
        <td></td>
    </tr>
    <tr>
        <td colspan="3">
            <p style="margin-bottom: 0;">
                If you did not request or expect this invitation, you can safely ignore this email.
            </p>
            <p style="margin-bottom: 0;">
                Best regards,
            </p>
            <p style="margin-bottom: 0; margin-top: 0;">
                Nam Nguyen - Sigil
            </p>
        </td>
    </tr>
</table>
</body>
</html>
