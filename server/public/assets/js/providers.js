$(document).ready(() => {
    var table = $('.table');
    var tableContent = $('#table-content');
    var loading = $('#loading');

    table.hide();
    db.collection('euru_pending_approvals').get()
        .then((snapshot) => {
            console.log(snapshot);
            
            if (snapshot && !snapshot.empty) {
                snapshot.forEach(doc => {
                    console.log(doc.data());
                    loading.hide();
                    table.show();

                    var user = doc.data();

                    tableContent.append(`
                        <tr>
                            <td>
                                
                                ${user.name}
                            </td>
                            <td>
                                ${new Date(user.timestamp).toDateString()}
                            </td>
                            <td>
                                ${user.key}
                            </td>
                            <td>
                                ${user.type === 'TYPE_CUSTOMER' ? 'Customer' : 'Service Provider'}
                            </td>
                            <td class="text-center">
                                <a href="#user" class="btn btn-info">Copy Key</a>
                            </td>
                        </tr>
                        `);
                });
            } else {
                table.hide();
                loading.text('Could not find any pending registrations...')
                loading.show();
            }
        }).catch((err) => {
            console.log(err.message);
        });
})